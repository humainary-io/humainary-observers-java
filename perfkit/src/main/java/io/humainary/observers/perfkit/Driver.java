/*
 * Copyright © 2022 JINSPIRED B.V.
 */

package io.humainary.observers.perfkit;

import io.humainary.devkit.perfkit.PerfKit;
import io.humainary.observers.Observers;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;

import static io.humainary.observers.Observers.*;
import static io.humainary.substrates.Substrates.*;


@State ( Scope.Benchmark )
public class Driver implements
                    PerfKit.Driver {

  private static final Outlet< Long >     OUTLET     = Outlet.empty ();
  private static final Subscriber< Long > SUBSCRIBER = subscriber ( OUTLET );
  private static final Name               NAME       = name ( "observer#1" );

  private Observers.Context< Long > context;
  private Observer< Long >          observer;

  @Setup ( Level.Trial )
  public final void setup ()
  throws IOException {

    final var configuration =
      configuration ();

    //noinspection RedundantTypeArguments
    context =
      context (
        hub (),
        Event< Long >::emittance,
        optic (),
        environment (
          lookup (
            path ->
              configuration.apply (
                path.toString ()
              )
          )
        )
      );

    context.subscribe (
      SUBSCRIBER
    );

    observer =
      context.observer (
        NAME
      );

  }

  @Benchmark
  public void context_get () {

    context.get (
      NAME
    );

  }

  @Benchmark
  public void context_observer () {

    context.observer (
      NAME
    );

  }

  @Benchmark
  public void context_subscribe_cancel () {

    context.subscribe (
      SUBSCRIBER
    ).close ();

  }

  @Benchmark
  public void context_consume_cancel () {

    context.consume (
      OUTLET
    ).close ();

  }

  @Benchmark
  public void context_iterator () {

    context.iterator ();

  }


  @Benchmark
  public void context_foreach () {

    for ( final Observer< Long > c : context ) {
      assert c != null;
    }

  }


  @Benchmark
  public void observer_observe () {

    observer.observe ();

  }

}