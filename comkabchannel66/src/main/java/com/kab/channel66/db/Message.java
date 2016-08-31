package com.kab.channel66.db;

public class Message {
        private long id;
        private String comment;
        private long date;

        public long getId() {
                return id;
        }

        public void setId(long id) {
                this.id = id;
        }

        public String getComment() {
                return comment;
        }

        public long getDate() {
                return date;
        }

        public void setDate(long date)
        {
               this.date = date;
        }
        public void setComment(String comment) {
                this.comment = comment;
        }

        // Will be used by the ArrayAdapter in the ListView
        @Override
        public String toString() {
                return comment;
        }
}