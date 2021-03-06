package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.impl.function.Function;
import org.meridor.perspective.sql.impl.function.FunctionName;
import org.meridor.perspective.sql.impl.function.FunctionsAware;
import org.meridor.perspective.sql.impl.table.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.meridor.perspective.sql.impl.expression.ExpressionUtils.*;

@Component
public class ExpressionEvaluatorImpl implements ExpressionEvaluator {
    
    private static final Logger LOG = LoggerFactory.getLogger(ExpressionEvaluator.class);
    
    @Autowired
    private FunctionsAware functionsAware;

    @Override
    public Map<String, Set<String>> getColumnNames(Object expression) {
        if (isColumnExpression(expression)) {
            ColumnExpression columnExpression = asColumnExpression(expression);
            String tableAlias = columnExpression.getTableAlias();
            String columnName = columnExpression.getColumnName();
            return Collections.singletonMap(tableAlias, Collections.singleton(columnName));
        } else if (expression instanceof FunctionExpression) {
            final Map<String, Set<String>> ret = new HashMap<>();
            FunctionExpression functionExpression = (FunctionExpression) expression;
            functionExpression.getArgs().stream()
            .map(this::getColumnNames)
            .forEach(m -> m.keySet().forEach(k -> {
                ret.putIfAbsent(k, new LinkedHashSet<>());
                ret.get(k).addAll(m.get(k));
            }));
            return ret;
        } else if (expression instanceof UnaryArithmeticExpression) {
            return getColumnNames(((UnaryArithmeticExpression) expression).getValue());
        } else if (expression instanceof BinaryArithmeticExpression) {
            BinaryArithmeticExpression binaryArithmeticExpression = (BinaryArithmeticExpression) expression;
            Map<String, Set<String>> leftColumnNames = getColumnNames(binaryArithmeticExpression.getLeft());
            Map<String, Set<String>> rightColumnNames = getColumnNames(binaryArithmeticExpression.getRight());
            return mergeColumnNames(leftColumnNames, rightColumnNames);
        }
        return Collections.emptyMap();
    }
    
    private static Map<String, Set<String>> mergeColumnNames(Map<String, Set<String>> left, Map<String, Set<String>> right) {
        return Stream.of(left, right)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> {
                    Set<String> both = new HashSet<>(a);
                    both.addAll(b);
                    return both;
                }));
    }
    
    @Override
    public <T extends Comparable<? super T>> T evaluate(Object expression, DataRow dataRow) {
        Assert.notNull(expression, "Expression can't be null");
        if (expression instanceof Null) {
            return null;
        } else if (isColumnExpression(expression)) {
            return evaluateColumnExpression((ColumnExpression) expression, dataRow);
        } else if (expression instanceof FunctionExpression) {
            return evaluateFunctionExpression((FunctionExpression) expression, dataRow);
        } else if (expression instanceof IsNullExpression) {
            return cast(evaluateIsNullExpression((IsNullExpression) expression, dataRow), Boolean.class, Comparable.class);
        } else if (expression instanceof InExpression) {
            return cast(evaluateInExpression((InExpression) expression, dataRow), Boolean.class, Comparable.class);
        } else if (expression instanceof LiteralBooleanExpression) {
            return cast(evaluateLiteralBooleanExpression((LiteralBooleanExpression) expression), Boolean.class, Comparable.class);
        } else if (expression instanceof SimpleBooleanExpression) {
            return cast(evaluateSimpleBooleanExpression((SimpleBooleanExpression) expression, dataRow), Boolean.class, Comparable.class);
        } else if (expression instanceof BinaryBooleanExpression) {
            return cast(evaluateBinaryBooleanExpression((BinaryBooleanExpression) expression, dataRow), Boolean.class, Comparable.class);
        } else if (expression instanceof UnaryBooleanExpression) {
            return cast(evaluateUnaryBooleanExpression((UnaryBooleanExpression) expression, dataRow), Boolean.class, Comparable.class);
        } else if (expression instanceof BinaryArithmeticExpression) {
            return evaluateBinaryArithmeticExpression((BinaryArithmeticExpression) expression, dataRow);
        } else if (expression instanceof UnaryArithmeticExpression) {
            return evaluateUnaryArithmeticExpression((UnaryArithmeticExpression) expression, dataRow);
        }
        return evaluateConstant(expression);
    }

    @Override
    public <T extends Comparable<? super T>> T evaluateAs(Object expression, DataRow dataRow, Class<T> cls) {
        Object result = evaluate(expression, dataRow);
        return result != null ? cast(result, result.getClass(), cls): null;
    }

    private <T extends Comparable<? super T>> T  evaluateAsOrDefault(Object expression, DataRow dataRow, Class<T> cls, T defaultValue) {
        if (expression == null) {
            return defaultValue;
        }
        T result = evaluateAs(expression, dataRow, cls);
        return (result != null) ? result : defaultValue; 
    }

    private <T extends Comparable<? super T>> T evaluateConstant(Object expression) {
        Class<?> expressionClass = expression.getClass();
        if (isConstant(expressionClass)) {
            return cast(expression, expressionClass, Comparable.class);
        }
        throw new IllegalArgumentException(String.format("Constant should be a string or a number but %s was given", expressionClass.getCanonicalName()));
    }

    
    private <T extends Comparable<? super T>> T evaluateColumnExpression(ColumnExpression columnExpression, DataRow dataRow) {
        String columnName = columnExpression.getColumnName();
        Object value = columnExpression.useAnyTable() ?
                dataRow.get(columnName) :
                dataRow.get(columnName, columnExpression.getTableAlias());
        return cast(value, Comparable.class);
    }

    private <T extends Comparable<? super T>> T evaluateFunctionExpression(FunctionExpression functionExpression, DataRow dataRow) {
        String functionName = functionExpression.getFunctionName();
        Optional<Function<?>> functionCandidate = functionsAware.getFunction(functionName);
        if (!functionCandidate.isPresent()) {
            throw new IllegalArgumentException(String.format("Function '%s' does not exist", functionName));
        }
        List<Object> passedArgs = functionExpression.getArgs(); //This one can contain expressions, so we try to evaluate them
        List<Object> realArgs = passedArgs.stream()
                .map(a -> evaluate(a, dataRow))
                .collect(Collectors.toList());
        Function<?> function = functionCandidate.get();
        Set<String> errors = function.validateInput(realArgs);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.stream().collect(Collectors.joining("; ")));
        }
        Object result = function.apply(realArgs);
        return cast(result, function.getReturnType(), Comparable.class);
    }

    private boolean evaluateInExpression(InExpression inExpression, DataRow dataRow) {
        if (inExpression == null) {
            return false;
        }
        Set<Object> candidates = inExpression.getCandidates();
        Set<String> candidatesAsConstants = candidates.stream()
                .filter(c -> c != null)
                .map(c -> (isConstant(c.getClass())) ? c : evaluate(c, dataRow))
                .map(String::valueOf)
                .collect(Collectors.toSet());
        if (candidates.isEmpty()) {
            return false;
        }
        Object value = inExpression.getValue();
        if (value == null) {
            return false;
        }
        Class<?> valueClass = value.getClass();
        if (!isConstant(valueClass)) {
            return evaluateInExpression(new InExpression(evaluate(value, dataRow), candidates), dataRow);
        }
        return candidatesAsConstants.contains(String.valueOf(value));
    }

    private Object evaluateIsNullExpression(IsNullExpression expression, DataRow dataRow) {
        FunctionExpression isNullExpression = new FunctionExpression(FunctionName.TYPEOF.name(), Arrays.asList(expression.getValue(), DataType.NULL));
        return evaluateFunctionExpression(isNullExpression, dataRow);
    }


    private boolean evaluateLiteralBooleanExpression(LiteralBooleanExpression literalBooleanExpression) {
        return literalBooleanExpression != null && literalBooleanExpression.getLiteral();
    }

    private boolean evaluateSimpleBooleanExpression(SimpleBooleanExpression simpleBooleanExpression, DataRow dataRow) {
        if (simpleBooleanExpression == null) {
            return false;
        }
        Object left = simpleBooleanExpression.getLeft();
        Object right = simpleBooleanExpression.getRight();
        BooleanRelation booleanRelation = simpleBooleanExpression.getBooleanRelation();
        if (oneOfIsNull(left, right)) {
            return false;
        }
        if (!isConstant(left.getClass())) {
            return evaluateSimpleBooleanExpression(new SimpleBooleanExpression(evaluate(left, dataRow), booleanRelation, right), dataRow);
        }
        if (!isConstant(right.getClass())) {
            return evaluateSimpleBooleanExpression(new SimpleBooleanExpression(left, booleanRelation, evaluate(right, dataRow)), dataRow);
        }
        Class<?> leftClass = left.getClass();
        Class<?> rightClass = right.getClass();
        if (bothAreNumbers(leftClass, rightClass)) {
            double leftAsDouble = asDouble(left);
            double rightAsDouble = asDouble(right);
            switch (booleanRelation) {
                case EQUAL:
                    return leftAsDouble == rightAsDouble; //This one can probably cause rounding problems when comparing integers
                case GREATER_THAN:
                    return leftAsDouble > rightAsDouble;
                case LESS_THAN:
                    return leftAsDouble < rightAsDouble;
                case GREATER_THAN_EQUAL:
                    return leftAsDouble >= rightAsDouble;
                case LESS_THAN_EQUAL:
                    return leftAsDouble <= rightAsDouble;
                case NOT_EQUAL:
                    return leftAsDouble != rightAsDouble;
                default:
                    throw new IllegalArgumentException(String.format(
                            "%s operation is not applicable to numbers: %f %s %f",
                            booleanRelation.getText(),
                            leftAsDouble,
                            booleanRelation.getText(),
                            rightAsDouble
                    ));
            }
        } else if (bothAreBooleans(leftClass, rightClass)) {
            boolean leftAsBoolean = asBoolean(left);
            boolean rightAsBoolean = asBoolean(right);
            switch (booleanRelation) {
                case EQUAL: return leftAsBoolean == rightAsBoolean;
                case NOT_EQUAL: return leftAsBoolean != rightAsBoolean;
                default:
                    throw new IllegalArgumentException(String.format(
                            "%s operation is not applicable to booleans: %s %s %s",
                            booleanRelation.getText(),
                            leftAsBoolean,
                            booleanRelation.getText(),
                            rightAsBoolean
                    ));
            }
        } else {
            String leftAsString = asString(left);
            String rightAsString = asString(right);
            switch (booleanRelation) {
                case EQUAL: return leftAsString.equals(rightAsString);
                case NOT_EQUAL: return !leftAsString.equals(rightAsString);
                case LIKE: {
                    String rightAsRegex = rightAsString
                            .replaceAll("(?<!\\\\)%", ".*") //We use look-behinds here (http://www.regular-expressions.info/lookaround.html)
                            .replaceAll("\\%", "%")
                            .replaceAll("(?<!\\\\)_", ".")
                            .replaceAll("\\_", "_");
                    return matchPattern(leftAsString, rightAsRegex);
                }
                case REGEXP: return matchPattern(leftAsString, rightAsString);
                default: throw new IllegalArgumentException(String.format(
                        "%s operation is not applicable to strings: '%s' %s '%s'",
                        booleanRelation.getText(),
                        leftAsString,
                        booleanRelation.getText(),
                        rightAsString
                ));
            }
        }
    }
    
    private static boolean matchPattern(String value, String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);
            return pattern.matcher(value).find();
        } catch (Exception e) {
            LOG.debug(String.format("Evaluating pattern expression to false as provided pattern \"%s\" is not correct", regex), e);
            return false;
        }
    }

    private boolean evaluateBinaryBooleanExpression(BinaryBooleanExpression binaryBooleanExpression, DataRow dataRow) {
        boolean left = evaluateAsOrDefault(binaryBooleanExpression.getLeft(), dataRow, Boolean.class, false);
        boolean right = evaluateAsOrDefault(binaryBooleanExpression.getRight(), dataRow, Boolean.class, false);
        switch (binaryBooleanExpression.getBinaryBooleanOperator()) {
            default:
            case AND: return left && right;
            case OR: return left || right;
            case XOR: return left ^ right;
        }
    }

    private boolean evaluateUnaryBooleanExpression(UnaryBooleanExpression unaryBooleanExpression, DataRow dataRow) {
        boolean value = evaluateAsOrDefault(unaryBooleanExpression.getValue(), dataRow, Boolean.class, false);
        switch (unaryBooleanExpression.getUnaryBooleanOperator()) {
            default:
            case NOT: return !value;
        }
    }

    private <T extends Comparable<? super T>> T evaluateBinaryArithmeticExpression(BinaryArithmeticExpression binaryArithmeticExpression, DataRow dataRow) {
        Object left = binaryArithmeticExpression.getLeft();
        Object right = binaryArithmeticExpression.getRight();
        BinaryArithmeticOperator binaryArithmeticOperator = binaryArithmeticExpression.getBinaryArithmeticOperator();
        if (oneOfIsNull(left, right)) {
            throw new IllegalArgumentException(String.format(
                    "Nulls are not allowed in arithmetic expressions: %s %s %s",
                    left,
                    binaryArithmeticOperator.getText(),
                    right
            ));
        }
        if (!isConstant(left.getClass())) {
            return evaluateBinaryArithmeticExpression(new BinaryArithmeticExpression(evaluate(left, dataRow), binaryArithmeticOperator, right), dataRow);
        }
        if (!isConstant(right.getClass())) {
            return evaluateBinaryArithmeticExpression(new BinaryArithmeticExpression(left, binaryArithmeticOperator, evaluate(right, dataRow)), dataRow);
        }
        Class<?> leftClass = left.getClass();
        Class<?> rightClass = right.getClass();
        if (!bothAreNumbers(leftClass, rightClass)) {
            throw new IllegalArgumentException(String.format(
                    "Arithmetic expression can contain only numbers: %s %s %s",
                    left,
                    binaryArithmeticOperator.getText(),
                    right
            ));
        }
        switch (binaryArithmeticOperator) {
            default:
            case PLUS: return bothAreIntegers(leftClass, rightClass) ?
                    cast(asInt(left) + asInt(right), Integer.class, Comparable.class) :
                    cast(asDouble(left) + asDouble(right), Double.class, Comparable.class);
            case MINUS: return bothAreIntegers(leftClass, rightClass) ?
                    cast(asInt(left) - asInt(right), Integer.class, Comparable.class) :
                    cast(asDouble(left) - asDouble(right), Double.class, Comparable.class);
            case MULTIPLY: return bothAreIntegers(leftClass, rightClass) ?
                    cast(asInt(left) * asInt(right), Integer.class, Comparable.class) :
                    cast(asDouble(left) * asDouble(right), Double.class, Comparable.class);
            case DIVIDE: return bothAreIntegers(leftClass, rightClass) ?
                    cast(asInt(left) / asInt(right), Integer.class, Comparable.class) :
                    cast(asDouble(left) / asDouble(right), Double.class, Comparable.class);
            case MOD: return bothAreIntegers(leftClass, rightClass) ?
                    cast(asInt(left) % asInt(right), Integer.class, Comparable.class) :
                    cast(asDouble(left) % asDouble(right), Double.class, Comparable.class);
            case BIT_AND: {
                if (!bothAreIntegers(leftClass, rightClass)) {
                    throw new IllegalArgumentException(String.format(
                            "Bitwise AND operation is only applicable to integers: %s %s %s",
                            left,
                            binaryArithmeticOperator.getText(),
                            right
                    ));
                }
                return cast(asInt(left) & asInt(right), Integer.class, Comparable.class);
            }
            case BIT_OR: {
                if (!bothAreIntegers(leftClass, rightClass)) {
                    throw new IllegalArgumentException(String.format(
                            "Bitwise OR operation is only applicable to integers: %s %s %s",
                            left,
                            binaryArithmeticOperator.getText(),
                            right
                    ));
                }
                return cast(asInt(left) | asInt(right), Integer.class, Comparable.class);
            }
            case BIT_XOR: {
                if (!bothAreIntegers(leftClass, rightClass)) {
                    throw new IllegalArgumentException(String.format(
                            "Bitwise XOR operation is only applicable to integers: %s %s %s",
                            left,
                            binaryArithmeticOperator.getText(),
                            right
                    ));
                }
                return cast(asInt(left) ^ asInt(right), Integer.class, Comparable.class);
            }
            case SHIFT_LEFT: {
                if (!bothAreIntegers(leftClass, rightClass)) {
                    throw new IllegalArgumentException(String.format(
                            "Bitwise SHIFT LEFT operation is only applicable to integers: %s %s %s",
                            left,
                            binaryArithmeticOperator.getText(),
                            right
                    ));
                }
                return cast(asInt(left) << asInt(right), Integer.class, Comparable.class);
            }
            case SHIFT_RIGHT: {
                if (!bothAreIntegers(leftClass, rightClass)) {
                    throw new IllegalArgumentException(String.format(
                            "Bitwise SHIFT RIGHT operation is only applicable to integers: %s %s %s",
                            left,
                            binaryArithmeticOperator.getText(),
                            right
                    ));
                }
                return cast(asInt(left) >> asInt(right), Integer.class, Comparable.class);
            }
        }
    }
    
    private <T extends Comparable<? super T>> T evaluateUnaryArithmeticExpression(UnaryArithmeticExpression unaryArithmeticExpression, DataRow dataRow) {
        Object value = unaryArithmeticExpression.getValue();
        if (value == null) {
            throw new IllegalArgumentException("Nulls are not allowed in arithmetic expressions");
        }
        Class<?> valueClass = value.getClass();
        UnaryArithmeticOperator unaryArithmeticOperator = unaryArithmeticExpression.getUnaryArithmeticOperator();
        if (!isConstant(valueClass)) {
            return evaluateUnaryArithmeticExpression(new UnaryArithmeticExpression(evaluate(value, dataRow), unaryArithmeticOperator), dataRow);
        }
        if (!isNumber(valueClass)) {
            throw new IllegalArgumentException(String.format(
                    "Arithmetic expression can contain only numbers: %s %s",
                    unaryArithmeticOperator.getText(),
                    value
            ));
        }
        switch (unaryArithmeticOperator) {
            default:
            case PLUS: return isInteger(valueClass) ? 
                    cast(+asInt(value), Integer.class, Comparable.class) :
                    cast(+asDouble(value), Double.class, Comparable.class) ;
            case MINUS: return isInteger(valueClass) ?
                    cast(-asInt(value), Integer.class, Comparable.class) :
                    cast(-asDouble(value), Double.class, Comparable.class) ;
            case BIT_NOT: {
                if (!isInteger(valueClass)) {
                    throw new IllegalArgumentException(String.format(
                            "Bitwise NOT operation is only applicable to integers: %s %s",
                            unaryArithmeticOperator.getText(),
                            value
                    ));
                }
                return cast(~asInt(value), Integer.class, Comparable.class);
            }
        }
    }

    private <T extends Comparable<? super T>> T cast(Object value, Class<?> requiredSuperclass) {
        return cast(value, requiredSuperclass, requiredSuperclass);
    }
    
    private <T extends Comparable<? super T>> T cast(Object value, Class<?> columnType, Class<?> requiredSuperclass) {
        if (!requiredSuperclass.isAssignableFrom(columnType)) {
            throw new IllegalArgumentException(String.format("Column type \"%s\" should subclass \"%s\"", columnType.getCanonicalName(), requiredSuperclass.getCanonicalName()));
        }
        @SuppressWarnings("unchecked")
        Class<T> typedColumnType = (Class<T>) columnType;
        return typedColumnType.cast(value);
    }
    
}
