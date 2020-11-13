package io.github.joeslice.topk;

import com.clearspring.analytics.stream.Counter;
import com.clearspring.analytics.stream.StreamSummary;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.InputStreamReader;
import java.io.LineNumberReader;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;

public class TopK {

    public static void main(String[] args) throws Exception {
        final ArgumentParser parser = ArgumentParsers.newArgumentParser("topk", true);
        parser.addArgument("--count", "-n").help("Number to show, default 1 million").type(Integer.class).setDefault(1_000_000);
        parser.addArgument("--capacity").help("Capacity of the data structure, default 10 million").type(Integer.class).setDefault(10_000_000);
        parser.addArgument("--failOnFirstError").action(storeTrue()).setDefault(false).help("Fail fast when reporting errors, default false");
        parser.addArgument("--csv").action(storeTrue()).setDefault(false).help("Output csv");

        final Namespace namespace = parser.parseArgsOrFail(args);

        final int count = namespace.getInt("count");
        final int capacity = namespace.getInt("capacity");
        final boolean failOnFirstError = namespace.getBoolean("failOnFirstError");
        final boolean csv = namespace.getBoolean("csv");
        final StreamSummary<String> summary = new StreamSummary<>(capacity);

        try (final LineNumberReader reader = new LineNumberReader(new InputStreamReader(System.in))) {
            String line = reader.readLine();
            while (line != null) {
                summary.offer(line);
                line = reader.readLine();
            }
        }

        long maxErrorSeen = 0;

        for (Counter<String> counter : summary.topK(count)) {
            final long error = counter.getError();
            System.out.println(counter.getCount() + (csv ? "," : " ") + counter.getItem());
            if (failOnFirstError && error > 0) {
                System.err.println("FATAL: Counting error of " + error + " found.");
                System.exit(2);
            }

            maxErrorSeen = Math.max(maxErrorSeen, error);
        }

        if (maxErrorSeen > 0) {
            System.err.println("WARN: Max error " + maxErrorSeen);
            System.exit(1);
        }
    }
}
