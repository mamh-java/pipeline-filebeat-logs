package io.jenkins.plugins.elasticstacklogs;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.jenkins.plugins.elasticstacklogs.input.OpentelemetryLogsInput;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class OpentelemetryLogsInputCollectorTest {
  private static final Logger LOGGER = Logger.getLogger(OpentelemetryLogsInputCollectorTest.class.getName());

  private static final File workdir = new File("/tmp");
  private Server server;

  private class LogsService extends LogsServiceGrpc.LogsServiceImplBase {

    @Override
    public void export(ExportLogsServiceRequest request, StreamObserver<ExportLogsServiceResponse> responseObserver) {
      LOGGER.info("[Server] Log received :" + request.toString());
      responseObserver.onNext(ExportLogsServiceResponse.newBuilder().build());
      responseObserver.onCompleted();
    }
  }
  /* FIXME enable logs support on opentelemetry-collector */
  @Rule
  public GenericContainer otelCollector = new GenericContainer("otel/opentelemetry-collector-dev:latest")
    .withClasspathResourceMapping("otel-collector.yml", "/otel-collector.yml", BindMode.READ_ONLY)
    .withFileSystemBind(workdir.getAbsolutePath(), "/tmp", BindMode.READ_WRITE)
    .withCommand("--config /otel-collector.yml --log-level DEBUG --log-profile dev ")
    .withStartupTimeout(Duration.ofMinutes(1));

  @BeforeClass
  public static void requiresDocker() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable());
  }

  @Before
  public void setUp() throws IOException {
  }

  @Test
  public void testLog() throws IOException, InterruptedException {
    OpentelemetryLogsInput input = new OpentelemetryLogsInput("127.0.0.1", otelCollector.getMappedPort(4317));
    input.write("foo00");
    Thread.sleep(5000);
    assertEquals(1, input.getCount());
    assertTrue(otelCollector.getLogs().contains("foo00"));
    input.write("foo01");
    input.write("foo02");
    input.write("foo03");
    input.write("foo04");
    Thread.sleep(5000);
    assertTrue(otelCollector.getLogs().contains("foo01"));
    assertTrue(otelCollector.getLogs().contains("foo02"));
    assertTrue(otelCollector.getLogs().contains("foo03"));
    assertTrue(otelCollector.getLogs().contains("foo04"));
    assertEquals(2, input.getCount());
    for(int i=0; i<20; i++){
      input.write("foo" + i);
    }
    Thread.sleep(5000);
    for(int i=0; i<20; i++){
      assertTrue(otelCollector.getLogs().contains("foo" + i));
    }
    assertEquals(4, input.getCount());
  }

}
