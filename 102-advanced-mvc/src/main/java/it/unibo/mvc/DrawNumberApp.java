package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.module.ModuleDescriptor.Builder;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;
    private static final String PATH = "src/main/resources/config.yml";

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        final Configuration conf = readConfiguration(PATH);
        this.model = new DrawNumberImpl(conf.getMin(), conf.getMax(), conf.getAttempts());
    }

    private Configuration readConfiguration(String path) {
        final Configuration.Builder confBuilder = new Configuration.Builder();
        try (BufferedReader inStream = new BufferedReader(new InputStreamReader(new FileInputStream(PATH)))) {
            for (int i = 0; i < 3; i++) {
                final StringTokenizer tokenizer = new StringTokenizer(inStream.readLine(), ": ");
                switch (tokenizer.nextToken()) {
                    case "minimum":
                        confBuilder.setMin(Integer.parseInt(tokenizer.nextToken()));
                        break;
                    case "maximum":
                        confBuilder.setMax(Integer.parseInt(tokenizer.nextToken()));
                        break;
                    case "attempts":
                        confBuilder.setAttempts(Integer.parseInt(tokenizer.nextToken()));
                        break;
                }
            }
        } catch (final IOException e) {
            for (final DrawNumberView v: views) {
                v.displayError(e.getMessage());
            }
            e.printStackTrace();
        }
        return confBuilder.build();
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl());
    }

}
