package VehicleSimPackage;

//Imports------------------------------------------------------------------------------------------
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.*;

public class VehicleSim extends JPanel {
    //Variable Declerations------------------------------------------------------------------------
    static final DrawingManager panel = new DrawingManager();
    static final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    static final int width = gd.getDisplayMode().getWidth()-100;
    static final int height = gd.getDisplayMode().getHeight()-100;
    static final int pxPerMeter = height/20;

    static Timer timer;
    static ArrayList<String> keysPressed = new ArrayList<String>();

    static final double maxAcceleration = 3;
    static final double maxDeceleration  = 9.6;
    static final double wheelTurnSpeed  = 3;
    static final double deltaTime = 0.020;

    static final Image carImg = Toolkit.getDefaultToolkit().getImage("VehicleSimPackage/Charger.png");
    static final Image wheelImg = Toolkit.getDefaultToolkit().getImage("VehicleSimPackage/Wheel.png");

    static Car car = new Car(width/pxPerMeter/2, height/pxPerMeter/2, Math.toRadians(20), 5, 2.2, carImg);
    static Wheel frontWheel = new Wheel(1.5, Math.toRadians(40), 0.76, wheelImg);
    static Wheel rearWheel = new Wheel(-1.5, 0, 0.76, wheelImg);


    //Key Listener Setup---------------------------------------------------------------------------
    public VehicleSim() {
		KeyListener listener = new keyListener(); //Create a keylistener instance to add to the jframe
		addKeyListener(listener);
		setFocusable(true);
	}

    public class keyListener implements KeyListener { //create a keylistener class
		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) { //When a key is pressed, add it to the list of pressed keys if it is not already on there
            if (!keysPressed.contains(KeyEvent.getKeyText(e.getKeyCode()))) {
                keysPressed.add(KeyEvent.getKeyText(e.getKeyCode()));
                System.out.println(keysPressed);
            }
		}

		@Override
		public void keyReleased(KeyEvent e) { //When a key is released, remove it from the list of pressed keys
            try {
                keysPressed.remove(keysPressed.indexOf(KeyEvent.getKeyText(e.getKeyCode())));
                System.out.println(keysPressed);
            } catch (IndexOutOfBoundsException i) {
                System.err.println("keyReleased err: " + i);
            }
		}
	}

    //Main-----------------------------------------------------------------------------------------
    public static void main(String[] args) { 
        JFrame frame = new JFrame("CreateProject"); //Basic JFrame setup
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(width, height));

        frame.add(new VehicleSim());
        frame.add(panel);

        // frame.setIconImage(sub);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);  

        timer = new Timer((int)(deltaTime*1000), new ActionListener() { //Create a timer that runs every 20 ms to run the game loop
            public void actionPerformed(ActionEvent evt) {
                gameLogic();
                frame.repaint();
            }
        });
        timer.start();
    }


    //Game Logic-----------------------------------------------------------------------------------
    static void gameLogic () {
        if (keysPressed.contains("W")) {
            car.xVel += maxAcceleration*Math.cos(car.rotation_rad)*deltaTime;
            car.yVel += maxAcceleration*Math.sin(car.rotation_rad)*deltaTime;
        }
        if (keysPressed.contains("S")) {
            car.xVel = Math.max(car.xVel-maxDeceleration*Math.cos(car.rotation_rad)*deltaTime, 0);
            car.yVel = Math.max(car.yVel-maxDeceleration*Math.sin(car.rotation_rad)*deltaTime, 0);
        }
        if (keysPressed.contains("A")) {
            frontWheel.steerAngle = Math.min(frontWheel.steerAngle+deltaTime*wheelTurnSpeed, frontWheel.maxSteerRad);
        }
        else if (keysPressed.contains("D")) {
            frontWheel.steerAngle = Math.max(frontWheel.steerAngle-deltaTime*wheelTurnSpeed, -frontWheel.maxSteerRad);
        }
        else {
            frontWheel.steerAngle = Math.max(Math.abs(frontWheel.steerAngle)-deltaTime*wheelTurnSpeed, 0)*Math.signum(frontWheel.steerAngle);
        }

        car.xPos += car.xVel;
        car.yPos += car.yVel;
    }


    //Drawing Manager------------------------------------------------------------------------------
    static class DrawingManager extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            setBackground(Color.GRAY);

            drawRotatedImage(g2d, car.img, car.rotation_rad, (int)(car.xPos*pxPerMeter), height-(int)(car.yPos*pxPerMeter), (int)(car.lenght*pxPerMeter), (int)(car.width*pxPerMeter));
            drawRotatedImage(g2d, frontWheel.img, car.rotation_rad+frontWheel.steerAngle, (int)((car.xPos+frontWheel.xDistToCOM*Math.cos(car.rotation_rad))*pxPerMeter), height-(int)((car.yPos+frontWheel.xDistToCOM*Math.sin(car.rotation_rad))*pxPerMeter), (int)(frontWheel.diameter*pxPerMeter), (int)(frontWheel.diameter*pxPerMeter));
            drawRotatedImage(g2d, rearWheel.img, car.rotation_rad+rearWheel.steerAngle, (int)((car.xPos+rearWheel.xDistToCOM*Math.cos(car.rotation_rad))*pxPerMeter), height-(int)((car.yPos+rearWheel.xDistToCOM*Math.sin(car.rotation_rad))*pxPerMeter), (int)(rearWheel.diameter*pxPerMeter), (int)(rearWheel.diameter*pxPerMeter));
        
            g2d.drawLine(width/2, 0, width/2, height);
            g2d.drawLine(0, height/2, width, height/2);
        }
    }

    public static void drawRotatedImage(Graphics2D g2d, Image image, double angle_rad, int x, int y, int width, int height){  
        AffineTransform baseTransform = g2d.getTransform();
        g2d.setTransform(AffineTransform.getRotateInstance(-angle_rad, x, y));
        g2d.drawImage(image, x-width/2, y-height/2, width, height, null);
        g2d.setTransform(baseTransform);
    }  

    //Class definitions----------------------------------------------------------------------------
    static class Car {              
        public double xPos;
        public double yPos;
        public double xVel;
        public double yVel;
        public double rotation_rad;
        public double rotationalVel;
        public double lenght;
        public double width;
        public Image img;

        public Car(double xPos, double yPos, double rotation_rad, double lenght, double width, Image img) {
            this.xPos = xPos;
            this.yPos = yPos;
            this.xVel = 0;
            this.yVel = 0;
            this.rotation_rad = rotation_rad;
            this.rotationalVel = 0;
            this.lenght = lenght;
            this.width = width;
            this.img = img;
        }
    }

    static class Wheel {              
        public double xDistToCOM;
        public double steerAngle;
        public double maxSteerRad;
        public double slipAngle;
        public double lateralForce;
        public double diameter;
        public Image img;

        public Wheel(double xDistToCOM, double maxSteerRad, double diameter, Image img) {
            this.xDistToCOM = xDistToCOM;
            this.steerAngle = 0;
            this.maxSteerRad = maxSteerRad;
            this.slipAngle = 0;
            this.lateralForce = 0;
            this.diameter = diameter;
            this.img = img;
        }
    }

    static class Vector2 {              
        public double x;
        public double y;

        public Vector2(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
