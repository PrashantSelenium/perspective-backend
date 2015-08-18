package org.meridor.perspective.engine.impl;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationsAware;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class OperationsAwareImpl implements OperationsAware {
    
    private static final Logger LOG = LoggerFactory.getLogger(OperationsAwareImpl.class);
    
    @Autowired
    private ApplicationContext applicationContext;

    private final Map<OperationId, Object> operationInstances = new HashMap<>();
    
    private final Map<OperationId, Method> operationMethods = new HashMap<>();

    @PostConstruct
    private void init() {
        Map<String, Object> operationBeans = applicationContext.getBeansWithAnnotation(Operation.class);
        operationBeans.values().stream().forEach(bean -> {
            final Object realBean = getRealBean(bean);
            List<OperationId> operationId = getOperationIds(realBean);
            operationId.stream().forEach(id -> {
                Optional<Method> operationMethod = getOperationMethod(realBean);
                if (operationMethod.isPresent()) {
                    operationInstances.put(id, realBean);
                    this.operationMethods.put(id, operationMethod.get());
                    LOG.debug(
                            "Added operation class {} with cloud type = {} and operation type = {}",
                            realBean.getClass().getCanonicalName(),
                            id.getCloudType(),
                            id.getOperationType()
                    );
                } else {
                    LOG.warn("Skipping operation class {} because it contains no method marked as entry point.");
                }
            });

        });
    }
    
    private Object getRealBean(Object bean) {
        if (AopUtils.isAopProxy(bean)) {
            Advised advisedBean = (Advised) bean;
            try {
                return advisedBean.getTargetSource().getTarget();
            } catch (Exception e) {
                LOG.debug("Failed to process AOP proxied bean {}", bean);
            }
        }
        return bean;
    }

    private List<OperationId> getOperationIds(Object bean) {
        Operation operation = bean.getClass().getAnnotation(Operation.class);
        return Arrays.stream(operation.type())
                .map(t -> getOperationId(operation.cloud(), t))
                .collect(Collectors.toList());
    }

    private OperationId getOperationId(CloudType cloudType, OperationType operationType) {
        return new OperationId(cloudType, operationType);
    }
    
    private Optional<Method> getOperationMethod(Object bean) {
        return Arrays
                .stream(bean.getClass().getMethods())
                .filter(this::isOperationMethodValid)
                .findFirst();
    }
    
    private boolean isOperationMethodValid(Method method) {
        Class<?> cloudClass = method.getParameters()[0].getType();
        Class<?> consumerOsSupplierClass = method.getParameters()[1].getType();
        boolean cloudClassCorrect = Cloud.class.isAssignableFrom(cloudClass);
        boolean consumerSupplierClassCorrect = 
                Consumer.class.isAssignableFrom(consumerOsSupplierClass) || Supplier.class.isAssignableFrom(consumerOsSupplierClass);
        return method.isAnnotationPresent(EntryPoint.class) && method.getParameterCount() == 2 && cloudClassCorrect && consumerSupplierClassCorrect;
    }
    
    @Override
    public boolean isOperationSupported(CloudType cloudType, OperationType operationType) {
        OperationId operationId = getOperationId(cloudType, operationType);
        return operationMethods.containsKey(operationId);
    }

    @Override
    public <T> boolean consume(Cloud cloudType, OperationType operationType, Consumer<T> consumer) throws Exception {
        return doAct(cloudType, operationType, consumer);
    }
    
    @Override
    public <T> boolean supply(Cloud cloud, OperationType operationType, Supplier<T> supplier) throws Exception {
        return doAct(cloud, operationType, supplier);
    }

    private boolean doAct(Cloud cloud, OperationType operationType, Object consumerOrSupplier) throws Exception {
        OperationId operationId = getOperationId(cloud.getType(), operationType);
        Object operationInstance = operationInstances.get(operationId);
        Method method = operationMethods.get(operationId);
        Class<?> booleanClass = ClassUtils.forName("boolean", null);
        if (method.getReturnType().equals(booleanClass)) {
            return (boolean) method.invoke(operationInstance, cloud, consumerOrSupplier);
        }
        method.invoke(operationInstance, cloud, consumerOrSupplier);
        return true;
    }

    private static class OperationId {
        private final CloudType cloudType;
        private final OperationType operationType;

        public OperationId(CloudType cloudType, OperationType operationType) {
            this.cloudType = cloudType;
            this.operationType = operationType;
        }

        public CloudType getCloudType() {
            return cloudType;
        }

        public OperationType getOperationType() {
            return operationType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OperationId that = (OperationId) o;

            return cloudType == that.cloudType && operationType == that.operationType;

        }

        @Override
        public int hashCode() {
            return (cloudType.name() + operationType.name()).hashCode();
        }
    }

}
