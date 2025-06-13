package oopsucks.controller;

public abstract class BaseCommand<T> implements Command<T> {
    protected abstract T doExecute() throws CommandException;
    
    @Override
    public final T execute() throws CommandException {
        if (!validate()) {
            throw new ValidationException("Invalid input parameters");
        }
        return doExecute();
    }
    
    @Override
    public boolean validate() {
        return true; // Default implementation - override in subclasses
    }
}
