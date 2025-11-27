package it.unibo.mvc;

/**
 *
 */
public interface DrawNumberView {

    /**
     * @param observer the controller to attach
     */
    void setObserver(DrawNumberViewObserver observer);

    /**
     * This method is called before the UI is used. It should finalize its status (if needed).
     */
    void start();

    /**
     * Informs the user that the inserted number is not correct.
     */
    void numberIncorrect();

    /**
     * @param res the result of the last draw
     */
    void result(DrawResult res);

    /**
     * Displays to the user an error message.
     * 
     * @param message the message to be displayed
     */
    void displayError(String message);
}
