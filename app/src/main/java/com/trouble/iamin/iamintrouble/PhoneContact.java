package com.trouble.iamin.iamintrouble;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.SmsManager;

import java.util.ArrayList;

/**
 * Created by jeanro on 5/14/16.
 */
public class PhoneContact {
    private String name;
    private String email;
    private String phone;
    private String id;

    public PhoneContact()
    {
        name = "";
        email = "";
        phone = "";
        id = "";
    }

    public String getName()
    {
        return name;
    }

    public String getEmail()
    {
        return email;
    }

    public String getPhone()
    {
        return phone;
    }

    public String getID()
    {
        return id;
    }

    public void forceReload()
    {
        _all = null;
        _phoneable = null;
        _emailable = null;
    }

    public boolean hasEmail()
    {
        if(email.length() == 0)
        {
            return false;
        }
        return true;
    }

    public boolean hasPhone()
    {
        if(phone.length() == 0)
        {
            return false;
        }
        return true;
    }

    private static ArrayList<PhoneContact> _all;
    private static ArrayList<PhoneContact> _phoneable;
    private static ArrayList<PhoneContact> _emailable;

    public enum ContactFilter
    {
        All,
        Phone,
        Email
    }

    public static void preFetchContacts(final Context context) {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                PhoneContact.fetch(PhoneContact.ContactFilter.Phone, context);
            }
        })).start();
    }

    public static void sendSMSToContacts(String smsContent) {
        if (smsContent == null) return;
        if (_phoneable != null && _phoneable.size() > 0) {
            SmsManager smsManager = SmsManager.getDefault();
            for (PhoneContact contact : _phoneable) {
                smsManager.sendTextMessage(contact.getPhone(), null, smsContent, null, null);
            }
        }
    }

    public static ArrayList<PhoneContact> fetch(Context context)
    {
        return fetch(ContactFilter.All,context);
    }

    public static ArrayList<PhoneContact> fetch(ContactFilter filter, Context context)
    {
        if(_all == null || _emailable == null || _phoneable == null)
        {
            ContentResolver contentResolver = context.getContentResolver();

            ArrayList<PhoneContact> allContacts = new ArrayList<PhoneContact>();
            ArrayList<PhoneContact> phoneableContacts = new ArrayList<PhoneContact>();
            ArrayList<PhoneContact> emailableContacts = new ArrayList<PhoneContact>();

            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if(cursor.moveToFirst())
            {
                do
                {
                    PhoneContact contact = new PhoneContact();

                    contact.name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    contact.id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                    Cursor emails = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contact.id, null, null);
                    while (emails.moveToNext())
                    {
                        contact.email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        break;
                    }
                    emails.close();

                    if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                    {
                        Cursor phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ contact.id }, null);
                        while (phones.moveToNext())
                        {
                            contact.phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            break;
                        }
                        phones.close();
                    }

                    if(contact.phone.length() > 0)
                    {
                        phoneableContacts.add(contact);
                    }
                    if(contact.email.length() > 0)
                    {
                        emailableContacts.add(contact);
                    }
                    allContacts.add(contact);

                } while (cursor.moveToNext()) ;
            }

            _phoneable = phoneableContacts;
            _all = allContacts;
            _emailable = emailableContacts;
            cursor.close();
        }

        switch(filter)
        {
            case All:
                return _all;

            case Email:
                return _emailable;

            case Phone:
                return _phoneable;
        }

        return _all;
    }
}
