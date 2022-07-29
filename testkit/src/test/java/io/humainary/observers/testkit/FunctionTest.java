/*
 * Copyright © 2022 JINSPIRED B.V.
 */

package io.humainary.observers.testkit;

import io.humainary.observers.Observers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static io.humainary.devkit.testkit.TestKit.capture;
import static io.humainary.devkit.testkit.TestKit.recorder;
import static io.humainary.observers.Observers.operant;
import static io.humainary.observers.Observers.optic;
import static io.humainary.substrates.Substrates.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The test class for the {@link Subscriber} and {@link Subscription} interfaces.
 *
 * @author wlouth
 * @since 1.0
 */

final class FunctionTest {

  private static final Name N1 = name ( "name#1" );
  private static final Name N2 = name ( "name#2" );

  private Observers.Context< Integer > context;

  @BeforeEach
  void setup () {

    context =
      Observers.context (
        name -> new AtomicInteger (),
        optic (
          operant (
            AtomicInteger::incrementAndGet
          )
        ),
        environment ()
      );

  }


  @Test
  void subscribe () {

    final var recorder =
      recorder (
        context
      );

    recorder.start ();

    final var o1 =
      context.observer ( N1 );

    final var o2 =
      context.observer ( N2 );

    context.sync ();

    o1.observe ();
    o1.observe ();

    o2.observe ();
    o2.observe ();

    final var capture =
      recorder
        .stop ()
        .orElseThrow (
          AssertionError::new
        );

    assertEquals (
      capture (
        o1,
        1
      ).to (
        o1,
        2
      ).to (
        o2,
        1
      ).to (
        o2,
        2
      ),
      capture
    );

  }

}
