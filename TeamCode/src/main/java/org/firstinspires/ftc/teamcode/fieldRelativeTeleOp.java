package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "Field Relative Mecanum")
public class FieldRelativeTeleOp extends LinearOpMode {

    private DcMotor motorFL;
    private DcMotor motorFR;
    private DcMotor motorBL;
    private DcMotor motorBR;

    @Override
    public void runOpMode() throws InterruptedException {
        motorFL = hardwareMap.get(DcMotor.class, "motorFL");
        motorFR = hardwareMap.get(DcMotor.class, "motorFR");
        motorBL = hardwareMap.get(DcMotor.class, "motorBL");
        motorBR = hardwareMap.get(DcMotor.class, "motorBR");

        // This reverses the right side of the drivetrain
        // If the robot moves backwards when told to go forwards, the left side needs to be reversed
        motorFR.setDirection(DcMotor.Direction.REVERSE);
        motorBR.setDirection(DcMotor.Direction.REVERSE);

        IMU imu = hardwareMap.get(IMU.class, "imu");
        IMU.Parameters imuParameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        ));
        imu.initialize(imuParameters);

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {
            double leftY = -gamepad1.left_stick_y; // The Y stick is always inverted
            double leftX = gamepad1.left_stick_x;
            double rightX = gamepad1.right_stick_x;

            // Resset field orient
            if (gamepad1.start) {
                imu.resetYaw();
            }

            double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
            drive(leftY, leftX, rightX, botHeading);
        }
    }

    public void drive(double forward, double right, double turn, double heading) {
        // Rotate the forward and right movement in the opposite direction of the current heading
        double rotForward = forward * Math.cos(-heading) + right * Math.sin(-heading);
        double rotRight = -forward * Math.sin(-heading) + right * Math.cos(-heading);
        forward = rotForward;
        right = rotRight;

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
