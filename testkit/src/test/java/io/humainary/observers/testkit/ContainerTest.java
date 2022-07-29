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

final class ContainerTest {

  private static final Name N1   = name ( "name#1" );
  private static final Name N2   = name ( "name#2" );
  private static final Long ZERO = 0L;

  private Observers.Context< Long > inner;
  private Observers.Context< Long > outer;
  private Counters.Counter          c1;
  private Counters.Counter          c2;

  @BeforeEach
  void setup () {

    final var counters =
      Counters.context ();

    c1 = counters.counter ( N1 );
    c2 = counters.counter ( N2 );

    // observers that observe the
    // event behavior of counters

    inner =
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


    // observers that observe on-demand
    // the value of other observers

    outer =
      context (
        inner,
        optic (
          ( closure, observer ) ->
            observer.observe ( lens (), closure )
        ),
        environment ()
      );


  }


  @Test
  void subscribe () {

    final var recorder =
      recorder (
        outer
      );

    recorder.start ();

    c1.inc ();
    c2.inc ();

    c1.inc ();
    c2.inc ();

    inner.sync ();

    final var o1 =
      outer.observer ( N1 );

    final var o2 =
      outer.observer ( N2 );

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
