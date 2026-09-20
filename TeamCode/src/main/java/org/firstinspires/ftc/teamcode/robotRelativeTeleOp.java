package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "Robot Relative Meccanum")
public class RobotRelativeTeleOp extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "motorFL");
        DcMotor motorFR = hardwareMap.get(DcMotor.class, "motorFR");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "motorBL");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "motorBR");

        // This reverses the right side of the drivetrain
        // If the robot moves backwards when told to go forwards, the left side neeeds to be reversed
        motorFR.setDirection(DcMotor.Direction.REVERSE);
        motorBR.setDirection(DcMotor.Direction.REVERSE);

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {
            double leftY = -gamepad1.left_stick_y; // The Y stick is always inverted
            double leftX = gamepad1.left_stick_x;
            double rightX = gamepad1.right_stick_x;

            drive(leftY, leftX, rightX)
        }
    }

    public void drive(forward, right, turn) {
        // If any of the motors powers are out of the -1 to 1 range, we need to lower them to -1 to 1
        // By dividing by the largest motor power, we keep the ratio the same
        // Denominator is 1 if all powers are in the -1 to 1 range, otherwise it's the largest power
        double denominator = Math.max(Math.abs(forward) + Math.abs(right) + Math.abs(turn), 1);
        double powerFL = (forward + right + turn) / denominator;
        double powerFR = (forward - right - turn) / denominator;
        double powerBL = (forward - right + turn) / denominator;
        double powerBR = (forward + right - turn) / denominator;

        motorFL.setPower(powerFL);
        motorFR.setPower(powerFR);
        motorBL.setPower(powerBL);
        motorBR.setPower(powerBR);
    }
}
