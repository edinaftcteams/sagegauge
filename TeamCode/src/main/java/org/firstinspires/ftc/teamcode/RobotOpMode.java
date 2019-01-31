package org.firstinspires.ftc.teamcode.teleop;

import com.edinaftcrobotics.drivetrain.Mecanum;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robot.PieceOfCake;

@TeleOp(name="Teleop", group="Teleop")
public class RobotOpMode extends OpMode {
    private PieceOfCake robot = new PieceOfCake();
    private boolean intakeYPressed = false;
    private boolean intakeAPressed = false;
    private boolean previousY = false;
    private boolean previousA = false;
    private boolean intakeIsRunning = false;
    private boolean flipBPressed = false;
    private boolean bumpersPressed = false;
    private boolean intakeInToggledOn = false;
    private boolean intakeOutToggledOn = false;
    private boolean listenToGamePad1 = false;
    private boolean listenToGamePad2 = false;
    private Mecanum mecanum = null;
    private boolean TopFlipSet = false;

    @Override
    public void init(){
        robot.init(hardwareMap);


        mecanum = new Mecanum(robot.getFrontL(), robot.getFrontR(), robot.getBackL(), robot.getBackR(), true, telemetry);
    }

    @Override
    public void loop() {
        if (TopFlipSet == false){
            robot.getTopFlip().setPosition(1);

            TopFlipSet = true;
        }

        mecanum.Drive(gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x);

        telemetry.addData("Back Lift", "%d", robot.getBackLift().getCurrentPosition());
        telemetry.addData("Front Lift", "%d", robot.getFrontLift().getCurrentPosition());
        telemetry.addData("Front Flip", "%d", robot.getFrontFlip().getCurrentPosition());
        telemetry.addData("Lf, rf, lb, rb: ", "%d %d %d %d", robot.getFrontL().getCurrentPosition(),
                robot.getFrontR().getCurrentPosition(), robot.getBackL().getCurrentPosition(),
                robot.getBackR().getCurrentPosition());
        telemetry.addData("y:", "%s", gamepad1.y);
        telemetry.addData("a:", "%s", gamepad1.a);
        telemetry.addData("intakeYPressed", "%s", intakeYPressed);
        telemetry.addData("intakeAPressed", "%s", intakeAPressed);
        telemetry.addData("intakeInToggledOn", "%s", intakeInToggledOn);
        telemetry.addData("intakeOutToggledOn", "%s", intakeOutToggledOn);

        ProcessSlide();
        ProcessIntake();
        ProcessFrontFlip();
        ProcessLift();
        ProcessTopFlip();
        ProcessPower();

        telemetry.update();
    }
    private void ProcessSlide() {
        if ((gamepad1.left_trigger > 0)) {
            robot.getSlide().setPower(1);
        } else if ((gamepad1.right_trigger > 0)) {
            robot.getSlide().setPower(-.5);
        } else {
            robot.getSlide().setPower(0);
        }
    }

    private void ProcessIntake2() {
        // Get both controller's. Drivers using both at the same time is not predictable!
        intakeYPressed = gamepad1.y;
        intakeAPressed = gamepad1.a;   
        intakeYPressed = gamepad2.y;
        intakeAPressed = gamepad2.a;
       
        // Not running.
        if (!intakeIsRunning) {
            if (intakeAPressed && (previousA != intakeAPressed)) {
                // Start running out.
                robot.getIntake().setPower(-1); //TODO Set this to conform with what is considered "out".
                intakeIsRunning = true;
            }

            previousA = intakeAPressed;

            if (intakeYPressed && (previousY != intakeYPressed)) {
                // Start running in.
                robot.getIntake().setPower(1); //TODO Set this to conform with what is considered "in".
                intakeIsRunning = true;
            }

            previousY = intakeYPressed;
        }

        if (intakeIsRunning) {
                if (intakeAPressed && (previousA != intakeAPressed) || intakeYPressed && (previousY != intakeAPressed)) {
                    // Stop running..
                    robot.getIntake().setPower(0);
                 }
                 
            previousA = intakeAPressed;
            previousY = intakeYPressed;
        }
    }

    private void ProcessIntake() {
        boolean yValue = false;
        boolean aValue = false;

        if (gamepad1.a || gamepad1.y){
            listenToGamePad1 = true;
        } else if (gamepad2.a || gamepad2.y) {
            listenToGamePad2 = true;
        }

        if (listenToGamePad1) {
            yValue = gamepad1.y;
            aValue = gamepad1.a;
        } else if (listenToGamePad2) {
            yValue = gamepad2.y;
            aValue = gamepad2.a;
        }

        if (yValue && !intakeYPressed) {
            intakeYPressed = true;
            intakeAPressed = false;
        } else if (!yValue && intakeYPressed) {
            if (!intakeInToggledOn) {
                robot.getIntake().setPower(-1);  // TODO - negative this if it spins the wrong way
                intakeInToggledOn = true;
                intakeOutToggledOn = false;
            } else {
                robot.getIntake().setPower(0);
                intakeInToggledOn = false;
                intakeOutToggledOn = false;
            }

            listenToGamePad1 = false;
            listenToGamePad2 = false;
            intakeYPressed = false;
            intakeAPressed = false;
        } else if (aValue && !intakeAPressed) {
            intakeAPressed = true;
            intakeYPressed = false;
        } else if (!aValue && intakeAPressed) {
            if (!intakeOutToggledOn) {
                robot.getIntake().setPower(1); // TODO - negative this is if spinis the wrong way
                intakeInToggledOn = false;
                intakeOutToggledOn = true;
            } else {
                robot.getIntake().setPower(0);
                intakeInToggledOn = false;
                intakeOutToggledOn = false;
            }

            listenToGamePad1 = false;
            listenToGamePad2 = false;
            intakeAPressed = false;
            intakeYPressed = false;
        }
    }

    private void ProcessPower(){
        if (gamepad1.dpad_down){
            mecanum.SetCurrentPower(0.6);
        } else if (gamepad1.dpad_up){
            mecanum.SetCurrentPower(1.0);
        } else if(gamepad1.dpad_left){
            mecanum.SetCurrentPower(1.4);
        }
    }

    private void ProcessFrontFlip() {
        if (gamepad1.left_bumper || gamepad2.left_bumper) {
            robot.getFrontFlip().setPower(1);
        } else if (gamepad1.right_bumper || gamepad2.right_bumper) {
            robot.getFrontFlip().setPower(-1);
        } else {
            robot.getFrontFlip().setPower(0);
        }
    }

    private void ProcessLift() {
        robot.getBackLift().setPower(-gamepad2.left_stick_y); // TODO - change these by either flipping the negative or adding/removing a negative
        robot.getFrontLift().setPower(gamepad2.left_stick_y);
    }

    private void ProcessTopFlip() {
        if (gamepad2.left_trigger != 0) {
            robot.getTopFlip().setPosition(.32);
        } else if (gamepad2.right_trigger != 0) {
            robot.getTopFlip().setPosition(0);
        } else {
            robot.getTopFlip().setPosition(1);
        }
    }
}