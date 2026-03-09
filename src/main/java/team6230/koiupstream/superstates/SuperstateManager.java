package team6230.koiupstream.superstates;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import edu.wpi.first.epilogue.Logged;
import team6230.koiupstream.superstates.Superstate.DefaultStates;


public class SuperstateManager<E extends Enum<E>> {
    private final List<Consumer<E>> subscribers = new ArrayList<>();
    private final List<BooleanSupplier> subscribersReady = new ArrayList<>();
    @Logged
    private E currentState;
    @Logged
    private E currentWantedState;

    public SuperstateManager(E defaultValue) {
        this.currentState = defaultValue;
        this.currentWantedState = defaultValue;
    }

    public void subscribe(Consumer<E> callback, BooleanSupplier isReady) {
        subscribers.add(callback);
        subscribersReady.add(isReady);
    }

    public void setWantedSuperstate(E state) {
        if (this.currentState.equals(state)) return;

        this.currentWantedState = state;
        
        for (var subscriber : subscribers) {
            subscriber.accept(state);
        }
    }

    public void periodic() {
        if (this.currentState.equals(this.currentWantedState)) return;

        for (var subscriber : subscribersReady) {
            if (!subscriber.getAsBoolean()) return;
        }

        this.currentState = this.currentWantedState;
    }

    public E getSuperstate() {
        return currentState;
    }

    public E getWantedSuperstate() {
        return currentWantedState;
    }

    public boolean isAtSuperstate() {
        return currentState.equals(currentWantedState);
    }

    public boolean isDefault() {
        return currentState instanceof DefaultStates;
    }
}
