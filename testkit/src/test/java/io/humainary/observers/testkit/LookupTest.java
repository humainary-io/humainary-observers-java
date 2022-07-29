/*
 * Copyright © 2022 JINSPIRED B.V.
 */

package io.humainary.observers.testkit;

import io.humainary.observers.Observers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.humainary.devkit.testkit.TestKit.capture;
import static io.humainary.devkit.testkit.TestKit.recorder;
import static io.humainary.observers.Observers.optic;
import static io.humainary.substrates.Substrates.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The test class for the {@link Subscriber} and {@link Subscription} interfaces.
 *
 * @author wlouth
 * @since 1.0
 */

final class LookupTest {

  private static final Name N1 = name ( "name#1" );
  private static final Name N2 = name ( "name#2" );

  private Observers.Context< Integer > context;

  @BeforeEach
  void setup () {

    context =
      Observers.context (
        lookup ( name -> ( name == N1 ) ? 1 : 2 ),
        optic (),
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

    o1.observe ();
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
        o2,
        2
      ),
      capture
    );

  }

}
