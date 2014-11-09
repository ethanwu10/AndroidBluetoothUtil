package ethanwu10.github.bluetoothutil.nxtutil;

import ethanwu10.github.bluetoothutil.BluetoothSppClient;

/**
 * Controls motors on an NXT connected via Bluetooth (using a {@link ethanwu10.github.bluetoothutil.BluetoothSppClient})
 * @author Ethan
 */
public class NxtRemoteMotorController {
    private final BluetoothSppClient mBtSppClient;

    public static interface Motor {
        public static final byte A = 0;
        public static final byte B = 1;
        public static final byte C = 2;
        public static final byte ALL = (byte) 0xff;
    }
    public static interface MotorMode {
        public static final byte MOTORON   = 0x01;
        public static final byte BRAKE     = 0x02;
        public static final byte REGULATED = 0x04;
    }
    public static interface MotorRegMode {
        public static final byte IDLE        = 0x00;
        public static final byte MOTOR_SPEED = 0x01;
        public static final byte MOTOR_SYNC  = 0x02;
    }
    public static interface MotorRunState {
        public static final byte IDLE     = 0x00;
        public static final byte RAMPUP   = 0x10;
        public static final byte RUNNING  = 0x20;
        public static final byte RAMPDOWN = 0x40;
    }

    public static class MotorState {
        public byte motor;
        public byte power;
        public byte motorMode;
        public byte motorRegMode;
        public byte motorRunState;
        public int  motorTachoLimit; //WIP: DOES NOT WORK: DO NOT USE

        public MotorState() {}
    }

    public static class MotorStateBuilder {
        public enum RampMode {rampUp, rampDown, rampNone}

        private byte motor;
        private byte power;
        private boolean isRunning;
        private boolean doRegulation;
        private boolean doRegulateSpeed;
        private boolean doSync;
        private boolean doBrake;
        private byte turnRatio;
        private boolean doRamp;
        private RampMode rampMode;
        private int tachoTarget;

        public void setMotor(byte motor_in) {
            if (motor_in < 0 || motor_in > 2) {
                throw new IllegalArgumentException("MotorStateBuilder::setMotor: motor " + motor_in + " is not a valid motor");
            }
            motor = motor_in;
        }

        public void setPower(byte power_in) {
            if (power_in < -100 || power_in > 100) {
                throw new IllegalArgumentException("MotorStateBuilder::setPower: power level " + power_in + " is not a valid power");
            }
            power = power_in;
            isRunning = power != 0;
        }

        public void setBrake(boolean brake) {
            doBrake = brake;
        }

        public void setSync(boolean sync) {
            if (!sync && !doRegulateSpeed) {
                doRegulation = false;
            }
            if (sync) {
                doRegulation = true;
            }
            doSync = sync;
        }

        public void setSpeedRegulation(boolean regulation) {
            if (!regulation && !doSync) {
                doRegulation = false;
            }
            if (regulation) {
                doRegulation = true;
            }
            doRegulateSpeed = regulation;
        }

        public void setRampMode(RampMode rampMode) {
            this.rampMode = rampMode;
        }

        public MotorState create() {
            MotorState tmp = new MotorState();
            tmp.motor = motor;
            tmp.power = power;
            if (isRunning) {
                tmp.motorMode |= MotorMode.MOTORON;
                tmp.motorRunState |= MotorRunState.RUNNING;
                if (doRegulation) {
                    tmp.motorMode |= MotorMode.REGULATED;
                    if (doRegulateSpeed) {
                        tmp.motorRegMode |= MotorRegMode.MOTOR_SPEED;
                    }
                    if (doSync) {
                        tmp.motorRegMode |= MotorRegMode.MOTOR_SYNC;
                    }
                }
            }
            else {
                tmp.motorRegMode = MotorRegMode.IDLE;
                tmp.motorRunState = MotorRunState.IDLE;
            }
            if (doBrake) {
                tmp.motorMode |= MotorMode.BRAKE;
                tmp.motorMode |= MotorMode.MOTORON;
                tmp.motorRunState |= MotorRunState.RUNNING;
            }
            return tmp;
        }
    }

    public NxtRemoteMotorController(BluetoothSppClient sppClient) {
        mBtSppClient = sppClient;
    }

    /**
     * Sets the state of the specified motors
     * @param motorStates states of motors to update
     */
    public void setMotorStates(MotorState[] motorStates) {
        byte[][] messages = new byte[motorStates.length][];
        int messageNum = 0;
        for (MotorState motorState : motorStates) {
            /*               0     1            2     3     4     5     6     7     8     9     10    11    12    13*/
            /*                                             port  power mode  reg   turn  runst tacholimit-------------*/
            byte[] tmpMsg = {0x0c, 0x00, (byte) 0x80, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            tmpMsg[4] = motorState.motor;
            tmpMsg[5] = motorState.power;
            tmpMsg[6] = motorState.motorMode;
            tmpMsg[7] = motorState.motorRegMode;
            tmpMsg[9] = motorState.motorRunState;
            for (int i = 0; i < 4; i++) {
                tmpMsg[i+10] = (byte)(motorState.motorTachoLimit & (0xff << i));
            }
            messages[messageNum++] = tmpMsg;
        }
        byte[] writeBuf = new byte[motorStates.length * 14];
        int i = 0;
        for (byte[] tmp : messages) {
            System.arraycopy(tmp, 0, writeBuf, i, 14);
            i += 14;
        }
        write(writeBuf);
    }

    /**
     * Wrapper for {@link #setMotorStates(NxtRemoteMotorController.MotorState[])}
     * @param motorState motor state to update
     * @see #setMotorStates(NxtRemoteMotorController.MotorState[])
     */
    public void setMotorState(MotorState motorState) {
        MotorState[] motorStates = new MotorState[1];
        motorStates[0] = motorState;
        setMotorStates(motorStates);
    }

    protected void write(byte[] buf) {
        mBtSppClient.write(buf);
    }
    protected void write(byte buf) {
        mBtSppClient.write(buf);
    }
}
