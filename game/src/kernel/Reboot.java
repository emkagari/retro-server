package kernel;

import common.SocketManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Reboot {

    private static byte remainingHours, remainingMinutes,lastMinute,lastHour;

    public void initialize() {
        check();
    }

    public static boolean check() {
        Date date = Calendar.getInstance().getTime();

        int actualHour = Integer.parseInt(new SimpleDateFormat("HH").format(date));
        if(actualHour>12){
            actualHour -= 12;
        }

        int actualMinute = Integer.parseInt(new SimpleDateFormat("mm").format(date));
        int total = actualHour * 60 + actualMinute;
        double restant = 0;
        Boolean alreadylaunchonMin = false;

            restant = (12 * 60) - (total - (5 * 60));
            byte hour = (byte) (restant / 60);
            byte minute = (byte) (((restant / 60) - hour) * 60);

            Reboot.remainingHours = hour;
            Reboot.remainingMinutes = minute;

            switch (actualHour) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    Reboot.remainingHours -= 12;
                    break;
            }

            if(Reboot.remainingMinutes == Reboot.lastMinute && Reboot.remainingHours == Reboot.lastHour ){
                alreadylaunchonMin = true;
            }

            if(Reboot.remainingMinutes==0 && Reboot.remainingHours > 0 ) {
                if(!alreadylaunchonMin) {
                    if(Reboot.remainingHours > 1) {
                        SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;" + Reboot.remainingHours + " heures");
                    }
                    else{
                        SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;" + Reboot.remainingHours + " heure");
                    }
                    Reboot.lastMinute = Reboot.remainingMinutes;
                    Reboot.lastHour = Reboot.remainingHours;
                }
            }
            else if(Reboot.remainingHours==0 && Reboot.remainingMinutes==30){
                if(!alreadylaunchonMin) {
                    SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;" + Reboot.remainingMinutes+ " minutes");
                    Reboot.lastMinute = Reboot.remainingMinutes;
                    Reboot.lastHour = Reboot.remainingHours;
                }
            }
            else if(Reboot.remainingHours==0 && Reboot.remainingMinutes<=15){
                if(!alreadylaunchonMin) {
                    SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;" + Reboot.remainingMinutes+ " minutes");
                    Reboot.lastMinute = Reboot.remainingMinutes;
                    Reboot.lastHour = Reboot.remainingHours;
                }
            }
            else{}

        return (hour == 0 && minute == 0) || (actualHour == 4 && actualMinute == 59);
    }

    public static String toStr() {
        String im = "Im115;";
        if (Reboot.remainingHours == 0) {
            im += Reboot.remainingMinutes + (Reboot.remainingMinutes > 1 ? " minutes" : " minute");
        } else {
            im += Reboot.remainingHours + (Reboot.remainingHours > 1 ? " heures et " : " heure et ");
            im += Reboot.remainingMinutes + (Reboot.remainingMinutes > 1 ? " minutes" : " minute");
        }
        return im;
    }
}