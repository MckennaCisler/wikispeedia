package edu.brown.cs.jmrs.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Factory<E> {

  private Class<? extends E>       product;
  private Constructor<? extends E> constructor;
  private Object[]                 arguments;

  public Factory(Class<? extends E> product, Object... constructorArgs) {

    this.product = product;

    Class<?>[] argParams = new Class<?>[constructorArgs.length];
    for (int i = 0; i < constructorArgs.length; i++) {
      argParams[i] = constructorArgs[i].getClass();
    }

    try {
      constructor = product.getConstructor(argParams);
      arguments = constructorArgs;
    } catch (NoSuchMethodException | SecurityException e) {
      e.printStackTrace();
    }
  }

  public E get() {
    try {
      return constructor.newInstance(arguments);
    } catch (
        InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NullPointerException e) {
      e.printStackTrace();
      return null;
    }
  }

  public E getWithAdditionalArgs(Object... args) {

    Object[] fullArgs = new Object[arguments.length + args.length];

    Class<?>[] argParams = new Class<?>[args.length];
    for (int i = 0; i < fullArgs.length; i++) {
      if (i < arguments.length) {
        fullArgs[i] = arguments[i];
        argParams[i] = fullArgs[i].getClass();
      } else {
        fullArgs[i] = args[i - arguments.length];
        argParams[i] = fullArgs[i].getClass();
      }
    }

    try {
      return product.getConstructor(argParams).newInstance(fullArgs);
    } catch (
        NoSuchMethodException
        | SecurityException
        | InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException e) {
      e.printStackTrace();
      return null;
    }
  }

}
