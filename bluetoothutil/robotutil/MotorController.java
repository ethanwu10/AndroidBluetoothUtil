package ethanwu10.github.bluetoothutil.robotutil;

/**
 * Controls motors on a remote robot
 * @author Ethan
 */
public interface MotorController {

    /**
     * Sets the state of the specified motors
     * @param motorStates states of motors to update
     */
    public void setMotorStates(MotorState[] motorStates);

    /**
     * Wrapper for {@link #setMotorStates(ethanwu10.github.bluetoothutil.robotutil.MotorState[])}
     * @param motorState motor state to update
     * @see #setMotorStates(ethanwu10.github.bluetoothutil.robotutil.MotorState[])
     */
    public void setMotorState(MotorState motorState);

}
