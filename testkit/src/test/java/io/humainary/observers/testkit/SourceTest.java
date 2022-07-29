/*
 * Copyright © 2022 JINSPIRED B.V.
 */

package io.humainary.observers.testkit;

import io.humainary.counters.Counters;
import io.humainary.observers.Observers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.humainary.devkit.testkit.TestKit.capture;
import static io.humainary.devkit.testkit.TestKit.recorder;
import static io.humainary.observers.Observers.*;
import static io.humainary.substrates.Substrates.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The test class for the {@link Subscriber} and {@link Subscription} interfaces.
 *
 * @author wlouth
 * @since 1.0
 */

final class SourceTest {

  private static final Name N1   = name ( "name#1" );
  private static final Name N2   = name ( "name#2" );
  private static final Long ZERO = 0L;

  private Observers.Context< Long > observers;
  private Counters.Counter          c1;
  private Counters.Counter          c2;

  @BeforeEach
  void setup () {

    final var counters =
      Counters.context ();

    c1 = counters.counter ( N1 );
    c2 = counters.counter ( N2 );

    observers =
      context (
        counters,
        Event::emittance,
        optic (
          bootstrap ( ZERO ),
          lens (),
          operant ( total -> ++total )
        ),
        environment ()
      );

  }


  @Test
  void subscribe () {

    final var recorder =
      recorder (
        observers
      );

    recorder.start ();

    c1.inc ();
    c2.inc ();

    c1.inc ();
    c2.inc ();

    observers.sync ();

    final var o1 =
      observers.get ( N1 ).orElseThrow ( AssertionError::new );

    final var o2 =
      observers.get ( N2 ).orElseThrow ( AssertionError::new );

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
        2L
      ).to (
        o2,
        2L
      ),
      capture
    );

  }

}
