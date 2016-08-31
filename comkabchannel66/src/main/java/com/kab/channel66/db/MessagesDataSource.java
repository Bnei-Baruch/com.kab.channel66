package com.kab.channel66.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;

public class MessagesDataSource {

        // Database fields
        private SQLiteDatabase database;
        private MySQLiteHelper dbHelper;
        private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
                        MySQLiteHelper.COLUMN_MESSAGE, MySQLiteHelper.COLUMN_CREATED };

        public MessagesDataSource(Context context) {
                dbHelper = new MySQLiteHelper(context);
        }

        public void open() throws SQLException {
                database = dbHelper.getWritableDatabase();
        }

        public void close() {
                dbHelper.close();
        }

        public Message createComment(String comment) {
                ContentValues values = new ContentValues();
                values.put(MySQLiteHelper.COLUMN_MESSAGE, comment);
                values.put(MySQLiteHelper.COLUMN_CREATED, System.currentTimeMillis());
                long insertId = database.insert(MySQLiteHelper.TABLE_MESSAGES, null,
                                values);
                Cursor cursor = database.query(MySQLiteHelper.TABLE_MESSAGES,
                                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                                null, null, null);
                cursor.moveToFirst();
                Message newComment = cursorToComment(cursor);
                cursor.close();
                return newComment;
        }

        public void deleteComment(Message comment) {
                long id = comment.getId();
                System.out.println("Comment deleted with id: " + id);
                database.delete(MySQLiteHelper.TABLE_MESSAGES, MySQLiteHelper.COLUMN_ID
                                + " = " + id, null);
        }

        public void deleteAllMessages() {


                database.delete(MySQLiteHelper.TABLE_MESSAGES,null, null);
        }

        public List<Message> getAllComments() {
                List<Message> comments = new ArrayList<Message>();

                Cursor cursor = database.query(MySQLiteHelper.TABLE_MESSAGES,
                                allColumns, null, null, null, null, null);

                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                        Message comment = cursorToComment(cursor);
                        comments.add(comment);
                        cursor.moveToNext();
                }
                // make sure to close the cursor
                cursor.close();
                return comments;
        }

        private Message cursorToComment(Cursor cursor) {
                Message comment = new Message();
                comment.setId(cursor.getLong(0));
                comment.setComment(cursor.getString(1));
                comment.setDate(cursor.getLong(2));
                return comment;
        }
}