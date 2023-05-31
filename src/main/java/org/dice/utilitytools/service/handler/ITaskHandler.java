package org.dice.utilitytools.service.handler;

public interface ITaskHandler<T,U> {
    T handleTask(U input);
}
