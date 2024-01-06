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
    static final int pxPerMeter = height/10;

    static Timer timer;
    static ArrayList<String> keysPressed = new ArrayList<String>();

    static final double maxAcceleration = 3;
    static final double maxDeceleration  = 9.6;
    static final double deltaTime = 0.020;

    static final Image carImg = Toolkit.getDefaultToolkit().getImage("VehicleSimPackage/Charger.png");
    static final Image wheelImg = Toolkit.getDefaultToolkit().getImage("VehicleSimPackage/Charger.png");

    static Car car = new Car(width/pxPerMeter/2, height/pxPerMeter/2, Math.toRadians(20), 5, 2.2, carImg);
    static Wheel frontWheel = new Wheel(1.5, 40);
    static Wheel rearWheel = new Wheel(-1.5, 0);


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
        }
    }

    public static void drawRotatedImage(Graphics2D g2d, Image image, double angle_rad, int x, int y, int width, int height){  
        AffineTransform baseTransform = g2d.getTransform();
        g2d.setTransform(AffineTransform.getRotateInstance(-angle_rad, x, y));
        g2d.drawImage(image, x-image.getWidth(null)/2, y-image.getHeight(null)/2, width, height, null);
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
        public double maxSteerAngle;
        public double slipAngle;
        public double lateralForce;

        public Wheel(double xDistToCOM, double maxSteerAngle) {
            this.xDistToCOM = xDistToCOM;
            this.steerAngle = 0;
            this.maxSteerAngle = maxSteerAngle;
            this.slipAngle = 0;
            this.lateralForce = 0;
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
