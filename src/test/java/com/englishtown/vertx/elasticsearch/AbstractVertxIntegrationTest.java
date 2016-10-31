package com.englishtown.vertx.elasticsearch;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Emir Dizdarevic
 */
@RunWith(VertxUnitRunner.class)
public abstract class AbstractVertxIntegrationTest {

    protected Vertx vertx;

    protected final void deployVerticle(TestContext testContext, String verticleName, DeploymentOptions deploymentOptions) throws InterruptedException {
        vertx = Vertx.vertx();

        CountDownLatch latch = new CountDownLatch(1);
        vertx.deployVerticle(verticleName, deploymentOptions, result -> {
            if (result.failed()) {
                result.cause().printStackTrace();
                testContext.fail();
            }
            latch.countDown();
        });

        latch.await(10, TimeUnit.SECONDS);
    }

    protected final void destroyVerticle() throws Exception {
        vertx.close();
    }

}
