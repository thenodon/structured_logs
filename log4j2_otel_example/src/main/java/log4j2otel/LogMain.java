package log4j2otel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.instrumentation.log4j.appender.v2_17.OpenTelemetryAppender;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.ResourceAttributes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
/*
https://otlp-gateway-prod-eu-west-0.grafana.net/otlp
274015
glc_eyJvIjoiNDE5Mzg2IiwibiI6InN0YWNrLTI3NDAxNS1vdGxwLXdyaXRlLW90bHAiLCJrIjoieWJSNnRFV1YzV2pUZDIwZDRZNzgyMmQ2IiwibSI6eyJyIjoiZXUifX0=

export OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
export OTEL_EXPORTER_OTLP_ENDPOINT=https://otlp-gateway-prod-eu-west-0.grafana.net/otlp
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic Mjc0MDE1OmdsY19leUp2SWpvaU5ERTVNemcySWl3aWJpSTZJbk4wWVdOckxUSTNOREF4TlMxdmRHeHdMWGR5YVhSbExXOTBiSEFpTENKcklqb2llV0pTTm5SRlYxWXpWMnBVWkRJd1pEUlpOemd5TW1RMklpd2liU0k2ZXlKeUlqb2laWFVpZlgwPQ=="

OTEL_EXPORTER_OTLP_PROTOCOL="http/protobuf";OTEL_EXPORTER_OTLP_ENDPOINT="https://otlp-gateway-prod-eu-west-0.grafana.net/otlp";OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic Mjc0MDE1OmdsY19leUp2SWpvaU5ERTVNemcySWl3aWJpSTZJbk4wWVdOckxUSTNOREF4TlMxdmRHeHdMWGR5YVhSbExXOTBiSEFpTENKcklqb2llV0pTTm5SRlYxWXpWMnBVWkRJd1pEUlpOemd5TW1RMklpd2liU0k2ZXlKeUlqb2laWFVpZlgwPQ=="

*/
public class LogMain {

    private static final Logger logger = LogManager.getLogger(LogMain.class);
    private static OpenTelemetry initializeOpenTelemetry() throws IOException {
        Properties prop = new Properties();
        try {
            prop.load(LogMain.class.getClassLoader().getResourceAsStream("otel_resources.properties"));
        } catch (IOException e) {
            System.err.println("otel_resources.properties not found");
        }
        OpenTelemetrySdk sdk =
                OpenTelemetrySdk.builder()
                        .setTracerProvider(SdkTracerProvider.builder().setSampler(Sampler.alwaysOn()).build())
                        .setLoggerProvider(
                                SdkLoggerProvider.builder()
                                        .setResource(
                                                Resource.getDefault().toBuilder()
                                                        .put(ResourceAttributes.SERVICE_NAME, "log4j-example")
                                                        .put(ResourceAttributes.SERVICE_VERSION, prop.getProperty("version","unknown"))
                                                        .put("datacenter", prop.getProperty("datacenter","unknown"))
                                                        .put("env", prop.getProperty("env","unknown"))
                                                        .put("pnx_system", prop.getProperty("pnx_system","unknown"))
                                                        .put("pnx_component", prop.getProperty("pnx_component","unknown"))
                                                        .put("pnx_itcard", prop.getProperty("pnx_itcard","unknown"))
                                                        .build())
                                        .addLogRecordProcessor(
                                                BatchLogRecordProcessor.builder(
                                                                OtlpGrpcLogRecordExporter.builder()
                                                                        .setEndpoint("http://localhost:4317")
                                                                        .build())
                                                        .build())
                                        .build())
                        .build();

        // Add hook to close SDK, which flushes logs
        Runtime.getRuntime().addShutdownHook(new Thread(sdk::close));

        return sdk;
    }

    public static void main(String[] args) throws Exception {
        System.err.println("Example of log4j2 otlp with ThreadContext");

        // Initialize OpenTelemetry
        OpenTelemetry openTelemetry = initializeOpenTelemetry();
        // Install OpenTelemetry in log4j appender
        io.opentelemetry.instrumentation.log4j.appender.v2_17.OpenTelemetryAppender.install(openTelemetry);

        // Set MDC (Mapped Diagnostic Context) key-value pairs
        // This is where the dynamic context information is set
        ThreadContext.put("trace_id", "123");
        ThreadContext.put("client_ip", "192.168.1.100");
        ThreadContext.put("source", "authentication-service");
        try  {
            AuthUser authUser = new AuthUser("123456", "john.doe@example.com");

            // Call the doAuthenticate method
            if (authUser.doAuthenticate()) {
                authUser.doStuff();
            }
        } catch (Exception e) {
            logger.error("Failed whole operation", e);
        }
        finally {
            // Clear MDC after logging
            // Remember that the MDC is thread-bound, so it's important to clear it after logging
            // Since this is the "top" of the thread, it's important to clear it after logging
            ThreadContext.clearAll();
        }
    }
}

