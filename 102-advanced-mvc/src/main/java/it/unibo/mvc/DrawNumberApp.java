package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {

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

    private Configuration readConfiguration(final String path) {
        final Configuration.Builder confBuilder = new Configuration.Builder();
        try (BufferedReader inStream = new BufferedReader(
            new InputStreamReader(
                new FileInputStream(path), StandardCharsets.UTF_8))) {
            for (int i = 0; i < 3; i++) {
                final StringTokenizer tokenizer = new StringTokenizer(inStream.readLine(), ": ");
                switch (tokenizer.nextToken()) {
                    case "minimum":
                        confBuilder.withMin(Integer.parseInt(tokenizer.nextToken()));
                        break;
                    case "maximum":
                        confBuilder.withMax(Integer.parseInt(tokenizer.nextToken()));
                        break;
                    case "attempts":
                        confBuilder.withAttempts(Integer.parseInt(tokenizer.nextToken()));
                        break;
                    default:
                }
            }
        } catch (final IOException e) {
            for (final DrawNumberView v: views) {
                v.displayError(e.getMessage());
            }
            return new Configuration.Builder().build();
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
        } catch (final IllegalArgumentException e) {
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
    @SuppressFBWarnings(
        value = "DM_EXIT",
        justification = "Acceptable for exercising purposes."
    )
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
     * @throws FileNotFoundException if the configuration file cannot be fetched
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(), new PrintStreamView(System.out));
    }

}
