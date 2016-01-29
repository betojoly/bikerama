package br.com.imcom.bikerama;

/**
 * Created by BETO on 23/01/2016.
 */
public class ConstantsService {
    public interface ACTION {
        public static String MAIN_ACTION = "br.com.imcom.bikerama.action.main";
        public static String PREV_ACTION = "br.com.imcom.bikerama.action.prev";
        public static String PLAY_ACTION = "br.com.imcom.bikerama.action.play";
        public static String NEXT_ACTION = "br.com.imcom.bikerama.action.next";
        public static String STARTFOREGROUND_ACTION = "br.com.imcom.bikerama.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "br.com.imcom.bikerama.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
