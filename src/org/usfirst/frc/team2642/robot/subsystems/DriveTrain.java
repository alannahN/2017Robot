package org.usfirst.frc.team2642.robot.subsystems;

import edu.wpi.first.wpilibj.AnalogGyro;
import org.usfirst.frc.team2642.robot.RobotMap;
import org.usfirst.frc.team2642.robot.commands.drive.DifferentialDrive;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.command.PIDSubsystem;



public class DriveTrain extends PIDSubsystem {
	//Determines drive type
	private boolean isDriveStraight = true;

	//Drive
	DifferentialDrive robotdrive = new DifferentialDrive(RobotMap.left,
			RobotMap.right);

	//Encoders for drive
	Encoder lEncoder = new Encoder(RobotMap.lEncoder1, RobotMap.lEncoder2, false, Encoder.EncodingType.k4X);
	Encoder rEncoder = new Encoder(RobotMap.rEncoder1, RobotMap.rEncoder2, false, Encoder.EncodingType.k4X);

	AnalogGyro gyro = new AnalogGyro(RobotMap.gyro);
	
	public DriveTrain() {
		super(RobotMap.driveStraightP, RobotMap.driveStraightI, RobotMap.driveStraightD);
		disable();
		gyro.setSensitivity(0.0065);
	}

	//Standard driving
	public void drive(double y, double x){
		robotdrive.DifferentialDrive(-y, -x);
	}
	
	public void stop(){
		robotdrive.DifferentialDrive(0, 0);
	}

	public double getGyro() {
		return gyro.getAngle() % 360.0;
	}

	public void resetGyro() {
		gyro.reset();
	}
	
	public double getEncoderLeft(){
		System.out.println(lEncoder.getDistance());
		return lEncoder.getDistance();
	}
	
	public double getEncoderRight(){
		return rEncoder.getDistance();
	}
	
	public void resetBothEncoders(){
		lEncoder.reset();
		rEncoder.reset();
	}
	
	//Returns a given encoder value as inches
	public double encoderInches(double encoderValue){
		return encoderValue / 12.9;
	}
	
	//Returns the left encoder distance as inches
	public double leftEncoderInches(){
		return encoderInches(getEncoderLeft());
	}
	
	public double rightEncoderInches(){
		return encoderInches(getEncoderRight());
	}
	
	public double setDegrees(double degrees){
		return degrees * -0.26;
	}
	
	//Changes the autonomous driving type
	public void setIsDriveStraight(boolean state){
		isDriveStraight = state;
	}
	
	//Drives straight in autonomous with PID control
	public void driveStraight(double speed){
		double correction = 0.0;
		//Checks to see if the difference between the left and right is within a margin of error
		if(Math.abs(leftEncoderInches() - rightEncoderInches()) > RobotMap.driveForwardOffset){	
			//Left distance is less than right distance
			if(leftEncoderInches() < rightEncoderInches()){
				correction = -RobotMap.driveCorrection;
			}else{	//Right distance is less than left distance
				correction = RobotMap.driveCorrection;
			}
		}
		drive(-speed, correction);	//Drives with the correction value
	}
	
	//Turns in autonomous with PID control
	public void driveTurn(double speed){
		double correctionL = 0.0;
		double correctionR = 0.0;
		//Checks to see if the difference between the left and right is within a margin of error
		if(Math.abs(leftEncoderInches() + rightEncoderInches()) > RobotMap.driveForwardOffset){
			if(leftEncoderInches() > rightEncoderInches())	//Left distance is greater than right distance
				correctionR = -RobotMap.driveCorrection;
			}else{
				correctionL = -RobotMap.driveCorrection;
			}
		robotdrive.tankDrive(-speed - correctionL, speed - correctionR);	//Drives as a tank drive to correct turning drift
	}
	
	
	//Drives off of a controller by default
    public void initDefaultCommand() {
    	setDefaultCommand(new DifferentialDrive());
    }
    
	@Override
	protected double returnPIDInput() {
		if(isDriveStraight)
			return (rightEncoderInches() + leftEncoderInches()) / 2.0;	//Returns the average distance of both encoders
		else
			return (rightEncoderInches() - leftEncoderInches()) / 2.0;	//Returns the average distance of both encoders
	}

	@Override
	protected void usePIDOutput(double output) {	//Changes the output based on what driving we are doing
		if(isDriveStraight)
			driveStraight(output);		
		else
			driveTurn(output);
	}
}

