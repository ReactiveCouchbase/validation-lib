package org.reactivecouchbase.validation;

public class ValidationError {

    public final Paths.Path path;
    public final String message;

    public ValidationError(Paths.Path path, String message) {
        this.path = path;
        this.message = message;
    }

    public ValidationError(String message) {
        this.path = Paths.Root;
        this.message = message;
    }

    public ValidationError(Paths.Path path, Exception message) {
        this.path = path;
        this.message = message.getMessage();
    }

    public ValidationError(Exception message) {
        this.path = Paths.Root;
        this.message = message.getMessage();
    }

    public static ValidationError of(String message) {
        return new ValidationError(message);
    }

    public static ValidationError at(Paths.Path path, String message) {
        return new ValidationError(path, message);
    }

    public static ValidationError of(Exception message) {
        return new ValidationError(message);
    }

    public static ValidationError at(Paths.Path path, Exception message) {
        return new ValidationError(path, message);
    }

    @Override
    public String toString() {
        return "ValidationError @ ( " + path + " => " + message + " )";
    }
}
