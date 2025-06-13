package oopsucks.controller;

public interface Command<T> {
    T execute() throws CommandException;
    boolean validate();
}