package org.starloco.locos.login.packet;

import org.starloco.locos.login.LoginClient;
import org.starloco.locos.login.LoginClient.Status;

class Password {

    public static void verify(LoginClient client, String pass) {
        String password = decryptPassword(pass, client.getKey());

        // Comparaison directe en clair (pas de hash) — projet pédagogique.
        if (!password.equals(client.getAccount().getPass())) {
            client.send("AlEf");
            client.kick();
            return;
        }

        client.setStatus(Status.SERVER);
    }

    /**
     * Déchiffre le mot de passe envoyé par le client Dofus (algo client retro,
     * indépendant du hash DB). Le résultat est le mot de passe en clair tapé par
     * l'utilisateur, qui est ensuite comparé directement à la colonne `pass` de la DB.
     */
    private static String decryptPassword(String pass, String key) {
        if (pass.startsWith("#1"))
            pass = pass.substring(2);
        String chain = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";

        char PPass, PKey;
        int APass, AKey, ANB, ANB2, somme1, somme2;

        StringBuilder decrypted = new StringBuilder();

        for (int i = 0; i < pass.length(); i += 2) {
            PKey = key.charAt(i / 2);
            ANB = chain.indexOf(pass.charAt(i));
            ANB2 = chain.indexOf(pass.charAt(i + 1));

            somme1 = ANB + chain.length();
            somme2 = ANB2 + chain.length();

            APass = somme1 - (int) PKey;
            if (APass < 0)
                APass += 64;
            APass *= 16;

            AKey = somme2 - (int) PKey;
            if (AKey < 0)
                AKey += 64;

            PPass = (char) (APass + AKey);

            decrypted.append(PPass);
        }

        return decrypted.toString();
    }
}
