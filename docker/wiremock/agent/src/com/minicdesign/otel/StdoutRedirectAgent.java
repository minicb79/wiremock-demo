package com.minicdesign.otel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StdoutRedirectAgent {
    private static final ThreadLocal<Boolean> IN_LOGGING = ThreadLocal.withInitial(() -> false);

    public static void premain(String agentArgs, Instrumentation inst) {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        System.setOut(new PrintStream(new LoggingOutputStream(Logger.getLogger("stdout"), Level.INFO, originalOut), true));
        System.setErr(new PrintStream(new LoggingOutputStream(Logger.getLogger("stderr"), Level.WARNING, originalErr), true));
    }

    private static class LoggingOutputStream extends OutputStream {
        private final Logger logger;
        private final Level level;
        private final PrintStream originalStream;
        private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        public LoggingOutputStream(Logger logger, Level level, PrintStream originalStream) {
            this.logger = logger;
            this.level = level;
            this.originalStream = originalStream;
        }

        @Override
        public void write(int b) throws IOException {
            if (IN_LOGGING.get()) {
                originalStream.write(b);
                return;
            }
            if (b == '\n') {
                flushBuffer();
            } else {
                bos.write(b);
            }
        }

        private void flushBuffer() {
            byte[] bytes = bos.toByteArray();
            bos.reset();
            if (bytes.length > 0) {
                String message = new String(bytes).trim();
                if (!message.isEmpty()) {
                    IN_LOGGING.set(true);
                    try {
                        logger.log(level, message);
                    } finally {
                        IN_LOGGING.set(false);
                    }
                }
            }
        }

        @Override
        public void flush() throws IOException {
            flushBuffer();
            originalStream.flush();
        }
    }
}
