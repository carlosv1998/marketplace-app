package com.example.marketplace;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DataHelper extends SQLiteOpenHelper {
    public DataHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS usuario (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "email TEXT, " +
                "contra TEXT, " +
                "hora_inicio TEXT, " +
                "hora_fin TEXT)");

        db.execSQL("CREATE TABLE producto (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "precio INTEGER, " +
                "cantidad INTEGER, " +
                "latitud REAL, " +
                "longitud REAL, " +
                "id_usuario INTEGER, " +
                "FOREIGN KEY(id_usuario) REFERENCES usuario(id))");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS usuario");
        db.execSQL("DROP TABLE IF EXISTS producto");

        db.execSQL("CREATE TABLE IF NOT EXISTS usuario (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "email TEXT, " +
                "contra TEXT, " +
                "hora_inicio TEXT, " +
                "hora_fin TEXT)");

        db.execSQL("CREATE TABLE producto (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "precio INTEGER, " +
                "cantidad INTEGER, " +
                "latitud REAL, " +
                "longitud REAL, " +
                "id_usuario INTEGER, " +
                "FOREIGN KEY(id_usuario) REFERENCES usuario(id))");
    }
}
