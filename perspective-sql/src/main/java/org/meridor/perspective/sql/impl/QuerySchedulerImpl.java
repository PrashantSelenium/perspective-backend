package org.meridor.perspective.sql.impl;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.meridor.perspective.sql.SQLLexer;
import org.meridor.perspective.sql.SQLParser;
import org.meridor.perspective.sql.SQLParserBaseListener;
import org.meridor.perspective.sql.impl.task.Task;

import java.sql.SQLSyntaxErrorException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class QuerySchedulerImpl extends SQLParserBaseListener implements QueryScheduler {

    private class InternalErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            String message = String.format("Parse error: \"%s\" near \"%s\" at %d:%d", msg, String.valueOf(offendingSymbol), line, charPositionInLine);
            SQLSyntaxErrorException ex = new SQLSyntaxErrorException(message, e);
            exception = Optional.of(ex);
        }
    }

    private final String sql;

    private Optional<SQLSyntaxErrorException> exception = Optional.empty();
    
    private final Queue<Task> tasksQueue = new LinkedList<>(); 

    public QuerySchedulerImpl(String sql) {
        this.sql = sql;
    }

    @Override
    public Queue<Task> schedule() throws SQLSyntaxErrorException {
        CharStream input = new ANTLRInputStream(sql);
        ANTLRErrorListener errorListener = new InternalErrorListener();
        SQLLexer sqlLexer = new SQLLexer(input);
        sqlLexer.removeErrorListeners();
        sqlLexer.addErrorListener(errorListener);
        CommonTokenStream commonTokenStream = new CommonTokenStream(sqlLexer);
        SQLParser sqlParser = new SQLParser(commonTokenStream);
        sqlParser.removeErrorListeners();
        sqlParser.addErrorListener(errorListener);
        ParseTree parseTree = sqlParser.query();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, parseTree);
        if (exception.isPresent()) {
            throw exception.get();
        }
        return tasksQueue;
    }
    
}
