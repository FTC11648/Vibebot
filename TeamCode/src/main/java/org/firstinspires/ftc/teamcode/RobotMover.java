package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

public class RobotMover {

    DcMotor leftDrive;
    DcMotor rightDrive;
    DcMotor centerDrive;
    BNO055IMU imu;

    public Orientation lastAngles = new Orientation();
    public double globalAngle;

    static final double     COUNTS_PER_MOTOR_REV    = 1120 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 4.0 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.1415926535897932384626433832795);
    /* Declare OpMode members. */

    public RobotMover(DcMotor leftDrive, DcMotor rightDrive, DcMotor centerDrive, BNO055IMU imu)
    {
        this.imu = imu;

        this.leftDrive = leftDrive;
        this.rightDrive = rightDrive;
        this.centerDrive = centerDrive;

    }


    private void resetAngle() {
        lastAngles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        globalAngle = 0;

    }

    private double getAngle() {
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        double deltaAngle = angles.firstAngle - lastAngles.firstAngle;

        if (deltaAngle < -180)
            deltaAngle += 360;
        else if (deltaAngle > 180)
            deltaAngle -= 360;

        globalAngle += deltaAngle;

        lastAngles = angles;

        return globalAngle;
    }

    public void rotate(double degrees) {
        double  leftPower, rightPower;

        // restart imu movement tracking.
        resetAngle();

        double k = 0.025;

        double error = (degrees - getAngle())*k;

        // getAngle() returns + when rotating counter clockwise (left) and - when rotating
        //if (degrees < 0) {
        // turn right.
        leftPower = -error;
        rightPower = error;
        //}
        // else if (degrees > 0) {
        // turn left.
        //leftPower = -power;
        //rightPower = power;
        //}
        //else return;

        // set power to rotate.
        leftDrive.setPower(leftPower);
        leftDrive.setPower(leftPower);
        rightDrive.setPower(rightPower);
        rightDrive.setPower(rightPower);



        // rotate until turn is completed.
        /*if (degrees < 0) {
            // On right turn we have to get off zero first.
            while (opModeIsActive() && getAngle() == 0) {}

            while (opModeIsActive() && getAngle() > degrees) {}
        }*/

        // left turn.

        while (Math.abs(getAngle()-degrees) >= 1) {
            error = (degrees - getAngle())*k;

            leftPower = -error;
            rightPower = error;

            leftDrive.setPower(leftPower);
            leftDrive.setPower(leftPower);
            rightDrive.setPower(rightPower);
            rightDrive.setPower(rightPower);
        }

        // turn the motors off.
        leftDrive.setPower(0);
        leftDrive.setPower(0);
        rightDrive.setPower(0);
        rightDrive.setPower(0);


        // reset angle tracking on new heading.
        resetAngle();
    }

    public void encoderDrive(double speed,
                             double leftInches, double rightInches,
                             double centerInches) {
        int newLeftTarget;
        int newRightTarget;
        int newCenterTarget;

        // Ensure that the opmode is still active


            // Determine new target position, and pass to motor controller
            newLeftTarget = leftDrive.getCurrentPosition() + (int) (leftInches * COUNTS_PER_INCH);
            newRightTarget = rightDrive.getCurrentPosition() + (int) (rightInches * COUNTS_PER_INCH);
            newCenterTarget = centerDrive.getCurrentPosition() + (int) (centerInches * COUNTS_PER_INCH);

            leftDrive.setTargetPosition(newLeftTarget);
            rightDrive.setTargetPosition(newRightTarget);
            centerDrive.setTargetPosition(newCenterTarget);

            // Turn On RUN_TO_POSITION
            leftDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rightDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            centerDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);


            // reset the timeout time and start motion.
            leftDrive.setPower(Math.abs(speed));
            rightDrive.setPower(Math.abs(speed));
            centerDrive.setPower(Math.abs(speed));

            while((Math.abs(leftDrive.getCurrentPosition()-newLeftTarget))>5 ||
                    (Math.abs(centerDrive.getCurrentPosition()-newCenterTarget))>5 ||
                    (Math.abs(rightDrive.getCurrentPosition()-newRightTarget))>5 ) {
                //hi
            }

            // Stop all motion;
            leftDrive.setPower(0);
            rightDrive.setPower(0);
            centerDrive.setPower(0);


            // Turn off RUN_TO_POSITION
            leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            centerDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            //  sleep(250);   // optional pause after each move

    }
}
