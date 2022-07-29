/*
 * Copyright © 2022 JINSPIRED B.V.
 */

package io.humainary.observers.spi;

import io.humainary.observers.Observers.Context;
import io.humainary.observers.Observers.*;
import io.humainary.spi.Providers.Provider;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static io.humainary.substrates.Substrates.*;


/**
 * The service provider interface for the humainary observers runtime.
 * <p>
 * Note: An SPI implementation of this interface is free to override
 * the default methods implementation included here.
 *
 * @author wlouth
 * @since 1.0
 */

public interface ObserversProvider
  extends Provider {


  < C, O, V, R > Context< R > context (
    final Function< ? super Name, O > fn,
    final Optic< C, ? super O, ? super V, R > optic,
    final Environment environment
  );


  default < C, O, V, R > Context< R > context (
    final Lookup< O > lookup,
    final Optic< C, ? super O, ? super V, R > optic,
    final Environment environment
  ) {

    return
      context (
        function ( lookup ),
        optic,
        environment
      );

  }


  < C, O extends Component, V, R > Context< R > context (
    final Container< O > container,
    final Optic< C, ? super O, ? super V, R > optic,
    final Environment environment
  );


  < C, E, O, V, R > Context< R > context (
    final Source< E > source,
    final Function< ? super Event< E >, O > selector,
    final Optic< C, ? super O, ? super V, R > optic,
    final Environment environment
  );


  < C, O, V, R > Optic< C, O, V, R > optic (
    final Bootstrap< C, ? extends R > bootstrap,
    final Lens< C, ? super O, ? extends V > lens,
    final Operant< C, ? super V, R > operant
  );


  default < C, O, V > Optic< C, O, V, V > optic (
    final Lens< C, ? super O, ? extends V > lens
  ) {

    return
      optic (
        bootstrap (),
        lens,
        operant ()
      );

  }


  default < C, V, R > Optic< C, V, V, R > optic (
    final Operant< C, ? super V, R > operant
  ) {

    return
      optic (
        bootstrap (),
        lens (),
        operant
      );

  }


  default < O, C > Optic< C, O, O, O > optic () {

    return
      optic (
        lens ()
      );

  }


  default < V, C > Lens< C, V, V > lens (
    final Predicate< ? super V > predicate
  ) {

    return
      ( closure, observable ) ->
        predicate.test ( observable )
        ? observable
        : null;

  }


  default < C, O > Lens< C, O, O > lens () {

    return
      ( __, observable ) ->
        observable;

  }


  default < V, O, C > Lens< C, O, V > lens (
    final Function< ? super O, ? extends V > func
  ) {

    return
      ( __, observable ) ->
        func.apply (
          observable
        );

  }


  default < C, V > Operant< C, V, V > operant () {

    return
      operant (
        value
          -> value
      );

  }


  default < C, R, V > Operant< C, V, R > operant (
    final BiFunction< ? super R, ? super V, ? extends R > func
  ) {

    return
      ( __, prev, value ) ->
        func.apply (
          prev,
          value
        );

  }


  default < C, R, V > Operant< C, V, R > operant (
    final UnaryOperator< R > func
  ) {

    return
      operant (
        ( prev, __ ) ->
          func.apply (
            prev
          )
      );

  }


  default < R, V, C > Operant< C, V, R > operant (
    final Function< ? super V, ? extends R > func
  ) {

    return
      operant (
        ( __, value ) ->
          func.apply (
            value
          )
      );

  }


  default < C, R > Bootstrap< C, R > bootstrap (
    final R defValue
  ) {

    return
      ( closure, name ) ->
        defValue;

  }


  default < C, R > Bootstrap< C, R > bootstrap () {

    return
      ( closure, name ) ->
        null;

  }


}
