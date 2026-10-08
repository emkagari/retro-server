package common;

import area.map.GameCase;
import area.map.GameMap;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Encryptador {

    private final char[] HASH = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E',
            'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
            'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
    };

    private final char[] HEX_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public String decifrarMapData(String key2, String preData) {
        String key = key2;
        String data = preData;
        try {
            key = prepareKey(key);
            data = decypherData(preData, key, String.valueOf(checksum(key)));
        } catch (Exception ignored) {
        }
        return data;
    }

    public String prepareKey(String d) {
        StringBuilder loc3 = new StringBuilder();
        int loc4 = 0;
        while (loc4 < d.length()) {
            loc3.append((char) Integer.parseInt(d.substring(loc4, loc4 + 2), 16));
            loc4 += 2;
        }
        return unescape(loc3.toString());
    }

    private char checksum(String s) {
        int loc3 = 0;
        int loc4 = 0;
        while (loc4 < s.length()) {
            loc3 += s.codePointAt(loc4) / 16;
            loc4++;
        }
        return HEX_CHARS[loc3 / 16];
    }

    public String decypherData(String d, String k, String checksum) {
        int c = Integer.parseInt(checksum, 16) * 2;
        StringBuilder loc5 = new StringBuilder();
        int loc6 = k.length();
        int loc7 = 0;
        int loc9 = 0;
        while (loc9 < d.length()) {
            loc5.append((char) (Integer.parseInt(d.substring(loc9, loc9 + 2), 16)
                    ^ k.codePointAt((loc7 + c) % loc6)));
            loc7++;
            loc9 += 2;
        }
        return unescape(loc5.toString());
    }

    private String unescape(String s1) {
        String s = s1;
        try {
            s = URLDecoder.decode(s, StandardCharsets.UTF_8.toString());
        } catch (Exception ignored) {
        }
        return s;
    }

    public void decompilarMapaData(GameMap mapa) {
        try {
            boolean activo;
            boolean lineaDeVista;
            boolean tieneObjInteractivo;
            byte caminable;
            byte level;
            byte slope;
            short objInteractivo;
            short f = 0;
            while (f < mapa.getMapData().length()) {
                StringBuilder celdaData = new StringBuilder(mapa.getMapData().substring(f, f + 10));
                ArrayList<Byte> celdaInfo = new ArrayList<>();
                for (int i = 0; i < celdaData.length(); i++) {
                    celdaInfo.add(getNumeroPorValorHash(celdaData.charAt(i)));
                }
                activo = ((celdaInfo.get(0) & 32) >> 5) != 0;
                lineaDeVista = (celdaInfo.get(0) & 1) != 0;
                tieneObjInteractivo = ((celdaInfo.get(7) & 2) >> 1) != 0;
                caminable = (byte) ((celdaInfo.get(2) & 56) >> 3); // 0 = no, 1 = medio, 4 = si
                level = (byte) (celdaInfo.get(1) & 15);
                slope = (byte) ((celdaInfo.get(4) & 60) >> 2);
                objInteractivo = (short) (((celdaInfo.get(0) & 2) << 12)
                        + ((celdaInfo.get(7) & 1) << 12)
                        + (celdaInfo.get(8) << 6)
                        + celdaInfo.get(9));
                short celdaID = (short) (f / 10);
                boolean movemiento = true;
                if (caminable == 0) {
                    movemiento = false;
                }
                GameCase celda = new GameCase(
                        mapa,
                        celdaID,
                        movemiento,
                        lineaDeVista,
                        level,
                        slope,
                        activo,
                        tieneObjInteractivo ? objInteractivo : -1
                );
                mapa.addCases(celda);
                if (tieneObjInteractivo && objInteractivo != -1) {
                    if (mapa.trabajos != null) {
                        getTrabajosPorOI(objInteractivo, mapa.trabajos);
                    }
                }
                f = (short) (f + 10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte getNumeroPorValorHash(char c) {
        for (int a = 0; a < HASH.length; a++) {
            if (HASH[a] == c) {
                return (byte) a;
            }
        }
        return -1;
    }

    private void noRepetirEnArray(ArrayList<Integer> array, int i) {
        if (!array.contains(i)) {
            array.add(i);
            array.trimToSize();
        }
    }

    public void getTrabajosPorOI(int oi, ArrayList<Integer> array) {
        switch (oi) {
            case 7019:
                noRepetirEnArray(array, 23);
                break;
            case 7013:
                noRepetirEnArray(array, 17);
                noRepetirEnArray(array, 149);
                noRepetirEnArray(array, 148);
                noRepetirEnArray(array, 15);
                noRepetirEnArray(array, 16);
                noRepetirEnArray(array, 147);
                break;
            case 7018:
                noRepetirEnArray(array, 110);
                break;
            case 7028:
                noRepetirEnArray(array, 151);
                break;
            case 7022:
                noRepetirEnArray(array, 135);
                break;
            case 7023:
                noRepetirEnArray(array, 134);
                break;
            case 7024:
                noRepetirEnArray(array, 133);
                break;
            case 7025:
                noRepetirEnArray(array, 132);
                break;
            case 7001:
                noRepetirEnArray(array, 109);
                noRepetirEnArray(array, 27);
                break;
            case 7016:
            case 7014:
                noRepetirEnArray(array, 63);
                break;
            case 7015:
                noRepetirEnArray(array, 123);
                noRepetirEnArray(array, 64);
                break;
            case 7036:
                noRepetirEnArray(array, 165);
                noRepetirEnArray(array, 166);
                noRepetirEnArray(array, 167);
                break;
            case 7011:
                noRepetirEnArray(array, 13);
                noRepetirEnArray(array, 14);
                break;
            case 7037:
                noRepetirEnArray(array, 163);
                noRepetirEnArray(array, 164);
                break;
            case 7002:
                noRepetirEnArray(array, 32);
                break;
            case 7005:
                noRepetirEnArray(array, 48);
                break;
            case 7003:
                noRepetirEnArray(array, 101);
                break;
            case 7008:
            case 7009:
            case 7010:
                noRepetirEnArray(array, 12);
                noRepetirEnArray(array, 11);
                break;
            case 7039:
                noRepetirEnArray(array, 182);
                noRepetirEnArray(array, 171);
                break;
            case 7038:
                noRepetirEnArray(array, 169);
                noRepetirEnArray(array, 168);
                break;
            case 7007:
                noRepetirEnArray(array, 47);
                noRepetirEnArray(array, 122);
                break;
            case 7012:
                noRepetirEnArray(array, 18);
                noRepetirEnArray(array, 19);
                noRepetirEnArray(array, 20);
                noRepetirEnArray(array, 21);
                noRepetirEnArray(array, 65);
                noRepetirEnArray(array, 66);
                noRepetirEnArray(array, 67);
                noRepetirEnArray(array, 142);
                noRepetirEnArray(array, 143);
                noRepetirEnArray(array, 144);
                noRepetirEnArray(array, 145);
                noRepetirEnArray(array, 146);
                break;
            case 7020:
                noRepetirEnArray(array, 1);
                noRepetirEnArray(array, 113);
                noRepetirEnArray(array, 115);
                noRepetirEnArray(array, 116);
                noRepetirEnArray(array, 117);
                noRepetirEnArray(array, 118);
                noRepetirEnArray(array, 119);
                noRepetirEnArray(array, 120);
                break;
            case 7027:
                noRepetirEnArray(array, 156);
                break;
        }
    }
}
