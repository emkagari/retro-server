package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;
import game.scheduler.entity.WorldPub;
import kernel.Config;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PubData extends AbstractDAO<Object> {

    public PubData(HikariDataSource dataSource)
	{
		super(dataSource);
	}

    /**
     * Réparation des chaînes double-encodées UTF-8 stockées en DB (mojibake type
     * "bloquÃ©" au lieu de "bloqué"). Symptôme : le `é` (UTF-8 `C3 A9`) a été
     * interprété en Latin-1 puis ré-encodé en UTF-8 → `C3 83 C2 A9`. On inverse :
     * on encode la chaîne courante en Latin-1 (récupère les 2 vrais bytes UTF-8)
     * puis on la redécode en UTF-8.
     * No-op si la chaîne ne contient pas de marqueur mojibake (Ã / Â).
     */
    private static String fixMojibake(String s) {
        if (s == null || (s.indexOf('Ã') < 0 && s.indexOf('Â') < 0)) return s;
        try {
            return new String(s.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    @Override
    public void load(Object obj) {
        String query = "SELECT * FROM `pubs` WHERE `server` = " + Config.INSTANCE.getSERVER_ID() + ";";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    WorldPub.pubs.add(fixMojibake(RS.getString("data")));
                }
            }
        } catch (SQLException e) {
            sendError("PubData load", e);
        }
    }


    @Override
	public boolean update(Object t)	{
		return false;
	}
}
