package org.dice.utilitytools.service.transform;

public interface ITransform<T,U> {
    public T Transform (U input, String splitter) throws Exception;
}
