/*
 * Copyright © 2022 JINSPIRED B.V.
 */

package io.humainary.observers;

import io.humainary.observers.spi.ObserversProvider;
import io.humainary.spi.Providers;
import io.humainary.substrates.Substrates;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static io.humainary.substrates.Substrates.*;

/**
 * An open and extensible observatory framework for building, layering, and composing observers
 * and their state inspection models on top of protocol (primarily input) observability instruments.
 */

public final class Observers {

  private static final ObserversProvider PROVIDER =
    Providers.create (
      "io.humainary.observers.spi.factory",
      "io.inspectis.observers.spi.alpha.ProviderFactory",
      ObserversProvider.class
    );

  private Observers () {}


  /**
   * A context represents some configured boundary within a process space where instruments are managed.
   *
   * @param <R> the class type of the observation result
   */

  public interface Context< R >
    extends Substrates.Context< Observer< R >, R >,
            AutoCloseable {

    /**
     * Releases the underlying resources held by the context.
     */

    void close ();


    /**
     * Returns the {@link Observer} mapped to the specified {@code Name}
     *
     * @param name the {@code Name} to be used to look and possibly create the {@link Observer}
     * @return A non-null {@link Observer} reference
     * @see #get(Name)
     */

    Observer< R > observer (
      final Name name
    );


    /**
     * Causes the context to synchronize itself with the underlying observable
     * state space (source, container) in terms of the observers-to-observables.
     */

    void sync ();

  }


  /**
   * An interface that represents an instrument that observes some aspect of an observable.
   *
   * @param <R> the class type of the observation result
   */

  public interface Observer< R >
    extends Instrument {

    /**
     * The {@link Type} used to identify the interface of this referent type
     */

    Type TYPE = type ( Observer.class );


    /**
     * Performs an observation of the underlying observable object
     */

    void observe ();


    /**
     * @param lens    the lens used to capture an observed value from the underlying observable object
     * @param closure the closure to be passed to the lens when there exists an observable object
     * @param <V>     the class type of the observed value
     * @param <C>     the class type of the closure state
     * @return The value returned by the lens or null if there was observable
     */

    < C, V > V observe (
      Lens< C, ? super R, V > lens,
      Closure< C > closure
    );

  }


  /**
   * An interface that allows an {@link Optic} to manage state across multiple observations of the same observable.
   *
   * @param <T> the class type of the state managed by the closure
   * @see Bootstrap
   * @see Lens
   * @see Operant
   * @see Optic
   */

  public interface Closure< T > {

    /**
     * Sets the value of the state managed by this closure.
     *
     * @param value the new state of the closure
     */

    void set (
      T value
    );


    /**
     * Returns the current state managed by this closure.
     *
     * @return the current value of the state
     */

    T get ();

  }


  /**
   * An interface that allows for the setting up of the {@link Closure} state and/or the initial observation result.
   *
   * @param <C> the class type of the closure state
   * @param <R> the class type of the initial result
   * @see Optic
   */

  @FunctionalInterface
  public interface Bootstrap< C, R > {

    /**
     * A method used to initialize the {@link Closure } and set an initial (default) observation result.
     *
     * @param name    the name of the observer
     * @param closure the closure associated with the observable object
     * @return the initial observation result
     */

    R initialize (
      Closure< C > closure,
      Name name
    );

  }


  /**
   * An interface used to capture an observed value from an observable object (referent or emittance).
   *
   * @param <C> the class type of the closure state
   * @param <O> the class type of the observable object
   * @param <V> the class type of the observed value
   */

  @FunctionalInterface
  public interface Lens< C, O, V > {

    /**
     * Capture an observed value from an observable object.
     *
     * @param closure    the closure associated with the observable object
     * @param observable the observable object
     * @return The observed value captured
     */

    V capture (
      final Closure< C > closure,
      final O observable
    );


    /**
     * Returns a lens that first applies this lens and then the specified `after` lens
     *
     * @param after the lens to be applied after the capture from this lens
     * @param <T>   the class type of the post-capture lens observation result
     * @return A lens that firsts applies this lens and then the `after` lens
     */

    default < T > Lens< C, O, T > pipe (
      final Lens< C, ? super V, ? extends T > after
    ) {

      return
        ( closure, observable ) ->
          after.capture (
            closure,
            capture (
              closure,
              observable
            )
          );

    }


    /**
     * Returns a lens that first tests a predicate before performing the capture with this lens.
     *
     * @param predicate the predicate to be called in determining whether to apply the lens
     * @return A lens that firsts checks a predicate before the lens is applied to the target
     */

    default Lens< C, O, V > require (
      final Predicate< ? super O > predicate
    ) {

      return
        ( closure, observable ) ->
          predicate.test ( observable )
          ? capture ( closure, observable )
          : null;

    }

  }


  /**
   * An interface that is used to compose an observation using a closure (past state) and the latest observed value.
   *
   * @param <C> the class type of the closure state
   * @param <V> the class type of the observed value
   * @param <R> the class type of the observation result
   */

  @FunctionalInterface
  public interface Operant< C, V, R > {

    /**
     * Returns an observation result based on the new observed value and closure state
     *
     * @param closure the closure associated with the observable object
     * @param prev    the previous observation result
     * @param value   the latest observed value captured from the observable object
     * @return An observation result based on the new observed value and closure state
     */

    R compose (
      Closure< C > closure,
      R prev,
      V value
    );

  }


  /**
   * An interface that combines {@link Bootstrap}, {@link Lens}, and {@link Operant} interfaces.
   *
   * @param <C> the class type of the closure state
   * @param <O> the class type of the observable object
   * @param <V> the class type of the observed value
   * @param <R> the class type of the observation result
   */

  public interface Optic< C, O, V, R >
    extends Bootstrap< C, R >,
            Lens< C, O, V >,
            Operant< C, V, R >,
            Substrate {

  }


  /**
   * Returns a {@link Bootstrap} that returns {@code null} as the initial observation result for all observables.
   *
   * @param <C> the class type of the closure state
   * @param <R> the class type of the observation result
   * @return A bootstrap that returns {@code null} as the initial observation result.
   */

  public static < C, R > Bootstrap< C, R > bootstrap () {

    return
      PROVIDER.bootstrap ();

  }


  /**
   * Returns a {@link Bootstrap} that returns the same initial observation result for all observables.
   *
   * @param defValue the value to be used as the initial observation result
   * @param <C>      the class type of the closure state
   * @param <R>      the class type of the observation result
   * @return a bootstrap that returns the same initial observation result.
   */

  public static < C, R > Bootstrap< C, R > bootstrap (
    final R defValue
  ) {

    return
      PROVIDER.bootstrap (
        defValue
      );

  }


  /**
   * Returns a {@link Lens} that always returns its observable object.
   *
   * @param <C> the class type of the closure state
   * @param <O> the class type of the observable object
   * @return a lens that always returns its observable object
   */

  public static < C, O > Lens< C, O, O > lens () {

    return
      PROVIDER.lens ();

  }


  /**
   * Returns a {@link Lens} that applies the function to the observable object in creating an observed value
   *
   * @param <C> the class type of the closure state
   * @param <O> the class type of the observable object
   * @param <V> the class type of the observed value (input)
   * @return a lens that returns the value from the supplied function
   */

  public static < C, O, V > Lens< C, O, V > lens (
    final Function< ? super O, ? extends V > func
  ) {

    return
      PROVIDER.lens (
        func
      );

  }


  /**
   * Returns a {@link Lens} that returns the observable object if the predicate succeeds otherwise {@code null}.
   *
   * @param predicate the predicate to be tested against the observable
   * @param <C>       the class type of the closure state
   * @param <O>       the class type of the observable object
   * @return a lens that returns the observable object if the predicate succeeds otherwise {@code null}
   */

  public static < C, O > Lens< C, O, O > lens (
    final Predicate< ? super O > predicate
  ) {

    return
      PROVIDER.lens (
        predicate
      );

  }


  /**
   * Returns an {@link Operant} that always returns its observed value.
   *
   * @param <C> the class type of the closure state
   * @param <V> the class type of both the observed value (input) and observation result (output)
   * @return an operant that always returns its observed value (input)
   */

  public static < C, V > Operant< C, V, V > operant () {

    return
      PROVIDER.operant ();

  }


  /**
   * Returns an {@link Operant} that applies the function to the previous observation result and newly observed value (discarding the closure state).
   *
   * @param func the function to be applied to the prev result and observed value (drops the closure)
   * @param <C>  the class type of the closure state
   * @param <V>  the class type of the observed value
   * @param <R>  the class type of the observation result
   * @return An operant that applies the function to the prev result and newly observed value
   */

  public static < C, V, R > Operant< C, V, R > operant (
    final BiFunction< ? super R, ? super V, ? extends R > func
  ) {

    return
      PROVIDER.operant (
        func
      );

  }


  /**
   * Returns an {@link Operant} that applies the operator to the previous observation result (discarding both the closure state and observed value).
   *
   * @param operator the operator to be applied to the previous observation result
   * @param <C>      the class type of the closure state
   * @param <V>      the class type of the observed value
   * @param <R>      the class type of the observation result
   * @return An operant that applies the operator to the previous observation result
   */

  public static < C, V, R > Operant< C, V, R > operant (
    final UnaryOperator< R > operator
  ) {

    return
      PROVIDER.operant (
        operator
      );

  }


  /**
   * Returns an {@link Operant} that applies the function to the newly observed value (discarding the closure and previous value).
   *
   * @param func the function to be applied to the newly observed value
   * @param <C>  the class type of the closure state
   * @param <V>  the class type of the observed value
   * @param <R>  the class type of the observation result
   * @return An operant that applies the function to the newly observed value
   */

  public static < C, V, R > Operant< C, V, R > operant (
    final Function< ? super V, ? extends R > func
  ) {

    return
      PROVIDER.operant (
        func
      );

  }

  /**
   * Returns an {@link Optic} that returns the observable as the observation result.
   *
   * @param <C> the class type of the closure state
   * @param <O> the class type of the observable object
   * @return An optic that returns the observable as the observation result.
   */

  public static < C, O > Optic< C, O, O, O > optic () {

    return
      PROVIDER.optic ();

  }


  /**
   * Returns an {@link Optic} that uses the provided {@link Lens} to capture an observed value from an observable and return it.
   *
   * @param lens the lens used to capture an observed value from an observable object
   * @param <C>  the class type of the closure state
   * @param <O>  the class type of the observable object
   * @param <V>  the class type of the observed value
   * @return An optic that uses the provided lens and operant to perform to return the observed value.
   */

  public static < C, O, V > Optic< C, O, V, V > optic (
    final Lens< C, ? super O, ? extends V > lens
  ) {

    return
      PROVIDER.optic (
        lens
      );

  }


  /**
   * Returns an {@link Optic} that uses the provided uses the observable object as the observed value input into an {@link Operant}.
   *
   * @param operant the operant applied to the observable object
   * @param <C>     the class type of the closure state
   * @param <V>     the class type of the observable and observed value
   * @param <R>     the class type of the observation result
   * @return An optic that uses the provided operant to compose an observation result from an observable object
   */

  public static < C, V, R > Optic< C, V, V, R > optic (
    final Operant< C, ? super V, R > operant
  ) {

    return
      PROVIDER.optic (
        operant
      );

  }


  /**
   * Returns an {@link Optic} that uses the provided {@link Lens} to capture an observed value from an observable and then applies an {@link Operant}.
   *
   * @param lens    the lens used to capture an observed value from an observable object
   * @param operant the operant applied to the observed value returned by the lens
   * @param <C>     the class type of the closure state
   * @param <O>     the class type of the observable object
   * @param <V>     the class type of the observed value
   * @param <R>     the class type of the observation result
   * @return An optic that uses the provided lens and operant to perform.
   */

  public static < C, O, V, R > Optic< C, O, V, R > optic (
    final Bootstrap< C, ? extends R > bootstrap,
    final Lens< C, ? super O, ? extends V > lens,
    final Operant< C, ? super V, R > operant
  ) {

    return
      PROVIDER.optic (
        bootstrap,
        lens,
        operant
      );

  }


  /**
   * Creates a pull-based {@link Context} that sources the observables of {@link Observer observers} on-demand from a mapping {@code Function}.
   *
   * @param fn          the function used for mapping from name to observable objects
   * @param optic       the optic used to capture and compose observed values into an observation result
   * @param environment the environment used to configure the context
   * @param <C>         the class type of the closure state
   * @param <O>         the class type of the observable object
   * @param <V>         the class type of the observed value
   * @param <R>         the class type of the observation result
   * @return A context that sources observers from a {@code Container}.
   */

  public static < C, O, V, R > Context< R > context (
    final Function< ? super Name, O > fn,
    final Optic< C, ? super O, ? super V, R > optic,
    final Environment environment
  ) {

    return
      PROVIDER.context (
        fn,
        optic,
        environment
      );

  }


  /**
   * Creates a pull-based {@link Context} that sources the observables of {@link Observer observers} on-demand from a {@code Lookup}.
   *
   * @param lookup      the source lookup of the observable objects
   * @param optic       the optic used to capture and compose observed values into an observation result
   * @param environment the environment used to configure the context
   * @param <C>         the class type of the closure state
   * @param <O>         the class type of the observable object
   * @param <V>         the class type of the observed value
   * @param <R>         the class type of the observation result
   * @return A context that sources observers from a {@code Container}.
   */

  public static < C, O, V, R > Context< R > context (
    final Lookup< O > lookup,
    final Optic< C, ? super O, ? super V, R > optic,
    final Environment environment
  ) {

    return
      PROVIDER.context (
        lookup,
        optic,
        environment
      );

  }

  /**
   * Creates a pull-based {@link Context} that sources {@link Observer observers} from a {@code Container}.
   *
   * @param container   the source container of the observable objects
   * @param optic       the optic used to capture and compose observed values into an observation result
   * @param environment the environment used to configure the context
   * @param <C>         the class type of the closure state
   * @param <O>         the class type of the observable object
   * @param <V>         the class type of the observed value
   * @param <R>         the class type of the observation result
   * @return A context that sources observers from a {@code Container}.
   */

  public static < C, O extends Component, V, R > Context< R > context (
    final Container< O > container,
    final Optic< C, ? super O, ? super V, R > optic,
    final Environment environment
  ) {

    return
      PROVIDER.context (
        container,
        optic,
        environment
      );

  }


  /**
   * Creates a push-based {@link Context} that sources {@link Observer observers} from a {@code Source}
   * using the {@code Subscriber}, {@code Outlet}, and {@code Subscription} interfaces.
   *
   * @param source      the source emitting observables (events)
   * @param selector    the selector used to select a referent or emittance from an outlet callback
   * @param optic       the optic used to capture and compose observed values into an observation result
   * @param environment the environment used to configure the context
   * @param <E>         the class type of the observable events
   * @param <C>         the class type of the closure state
   * @param <O>         the class type of the observable object
   * @param <V>         the class type of the observed value
   * @param <R>         the class type of the observation result
   * @return A context that sources observers from a {@code Container}.
   */

  public static < C, E, O, V, R > Context< R > context (
    final Source< E > source,
    final Function< ? super Event< E >, O > selector,
    final Optic< C, ? super O, ? super V, R > optic,
    final Environment environment
  ) {

    return
      PROVIDER.context (
        source,
        selector,
        optic,
        environment
      );

  }

}