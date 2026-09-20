package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

@TeleOp(name = "Mecanum Drive")
public class MecanumTeleOp extends LinearOpMode {

    private ElapsedTime runtime = new ElapsedTime();

    private DcMotor motorFL;
    private DcMotor motorFR;
    private DcMotor motorBL;
    private DcMotor motorBR;
    private IMU imu;

    private YawPitchRollAngles robotOrientation;

    @Override
    public void runOpMode() throws InterruptedException {
        motorFL = hardwareMap.get(DcMotor.class, "motorFL");
        motorFR = hardwareMap.get(DcMotor.class, "motorFR");
        motorBL = hardwareMap.get(DcMotor.class, "motorBL");
        motorBR = hardwareMap.get(DcMotor.class, "motorBR");

        // This reverses the right side of the drivetrain
        // If the robot moves backwards when told to go forwards, the left side needs to be reversed
        motorFL.setDirection(DcMotor.Direction.FORWARD);
        motorFR.setDirection(DcMotor.Direction.REVERSE);
        motorBL.setDirection(DcMotor.Direction.FORWARD);
        motorBR.setDirection(DcMotor.Direction.REVERSE);

        motorFL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorFR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Reset encoders
        motorFL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorFR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorBL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorBR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        motorFL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        imu = hardwareMap.get(IMU.class, "imu");
        IMU.Parameters imuParameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        ));
        imu.initialize(imuParameters);

        boolean prevGamepad1Back = false;
        boolean useFieldRelative = true;

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();
        imu.resetYaw();
        runtime.reset();

        while (opModeIsActive()) {
            // Different types of slow modes

            // 25% speed when left bumper is pressed
            // double speedMult = gamepad1.left_bumper ? 0.25 : 1

            // Change speed based on trigger positions
            // 25% speed with right trigger full pressed, 5% speed with both full pressed
            double speedMult = 1 - gamepad1.right_trigger * 0.75 - gamepad1.left_trigger * 0.2;

            double leftY = speedMult * -gamepad1.left_stick_y; // The Y stick is always inverted
            double leftX = speedMult * gamepad1.left_stick_x;
            double rightX = speedMult * gamepad1.right_stick_x;

            // Reset field orient
            if (gamepad1.start) {
                imu.resetYaw();
            }
            // Toggle field orient
            if (gamepad1.back && !prevGamepad1Back) {
                useFieldRelative = !useFieldRelative;
            }

            // If rotation is needed earlier, call the robotOrientation function earlier
            robotOrientation = imu.getYawPitchRollAngles();
            double botHeading = robotOrientation.getYaw(AngleUnit.RADIANS);
            drive(leftY, leftX, rightX, botHeading, useFieldRelative);

            // Update all prev vars
            prevGamepad1Back = gamepad1.back;
        }
    }

    private void drive(double forward, double right, double turn, double heading, boolean useFieldRelative) {
        if (useFieldRelative) {
            // Rotate the forward and right movement in the opposite direction of the current heading
            double rotForward = forward * Math.cos(-heading) + right * Math.sin(-heading);
            double rotRight = -forward * Math.sin(-heading) + right * Math.cos(-heading);

            driveRobotRelative(rotForward, rotRight, turn);
        } else {
            driveRobotRelative(forward, right, turn);
        }
    }

    private void driveRobotRelative(double forward, double right, double turn) {
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
