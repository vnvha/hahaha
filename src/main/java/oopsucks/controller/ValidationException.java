package oopsucks.controller;

class ValidationException extends CommandException {
    public ValidationException(String message) {
        super(message);
    }
}