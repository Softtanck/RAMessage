package com.softtanck.model;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.softtanck.ramessage.IRaMessenger;
import com.softtanck.ramessageclient.core.engine.BaseClientHandler;
import com.softtanck.ramessageclient.core.util.ReflectionUtils;

/**
 * Created by Softtanck on 2022/3/12
 * Copied from Android's SDK. ref: Messenger.java
 */
public class RaCustomMessenger implements Parcelable {
    /**
     * For upgrading logic. And the default version is 1.
     * NOTE: This code needs to updated if the version of client changed.
     */
    public static final int RA_CLIENT_MESSENGER_VERSION = 1;
    public static final String RA_CLIENT_MESSENGER_KEY = "ra_client_messenger_key";
    public static final Pair<String, Integer> raMsgVersion = new Pair<>(RA_CLIENT_MESSENGER_KEY, RA_CLIENT_MESSENGER_VERSION);
    private final IRaMessenger mTarget;

    public RaCustomMessenger(Handler target) throws IllegalAccessException {
        MessageQueue messageQueueFromHandler = ReflectionUtils.INSTANCE.getMessageQueueFromHandler(target);
        if (messageQueueFromHandler != null) {
            synchronized (messageQueueFromHandler) {
                mTarget = new BaseClientHandler.RaCustomClientMessengerImpl(target);
            }
        } else {
            // Throw an exception if the target handler is not a client handler.
            throw new IllegalAccessException("The target handler is not a client handler.");
//            Object iMessengerFromSystem = ReflectionUtils.INSTANCE.getIMessengerFromSystem(target);
//            if (iMessengerFromSystem != null) {
//                mTarget = (IRaMessenger) iMessengerFromSystem;
//            }
        }
    }

    /**
     * Send a Message to this Messenger's Handler.
     *
     * @param message The Message to send.  Usually retrieved through
     *                {@link Message#obtain() Message.obtain()}.
     * @throws RemoteException Throws DeadObjectException if the target
     *                         Handler no longer exists.
     */
    public void send(Message message) throws RemoteException {
        mTarget.send(message);
    }

    /**
     * A method which can be used to send a message in sync.
     *
     * @param message The Message to send.  Usually retrieved through
     *                {@link Message#obtain() Message.obtain()}.
     * @return the result from service
     * @throws RemoteException Throws DeadObjectException if the target
     *                         Handler no longer exists.
     */
    public Message sendSync(Message message) throws RemoteException {
        return mTarget.sendSync(message);
    }

    /**
     * Retrieve the IBinder that this Messenger is using to communicate with
     * its associated Handler.
     *
     * @return Returns the IBinder backing this Messenger.
     */
    @Nullable
    public IBinder getBinder() {
        return mTarget == null ? null : mTarget.asBinder();
    }


    /**
     * Comparison operator on two Messenger objects, such that true
     * is returned then they both point to the same Handler.
     */
    public boolean equals(Object otherObj) {
        if (otherObj == null) {
            return false;
        }
        try {
            return mTarget.asBinder().equals(((RaCustomMessenger) otherObj)
                    .mTarget.asBinder());
        } catch (ClassCastException e) {
        }
        return false;
    }

    public int hashCode() {
        return mTarget.asBinder().hashCode();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(mTarget.asBinder());
    }

    public static final Creator<RaCustomMessenger> CREATOR = new Creator<RaCustomMessenger>() {
        public RaCustomMessenger createFromParcel(Parcel in) {
            IBinder target = in.readStrongBinder();
            return target != null ? new RaCustomMessenger(target) : null;
        }

        public RaCustomMessenger[] newArray(int size) {
            return new RaCustomMessenger[size];
        }
    };

    /**
     * Convenience function for writing either a Messenger or null pointer to
     * a Parcel.  You must use this with {@link #readMessengerOrNullFromParcel}
     * for later reading it.
     *
     * @param messenger The Messenger to write, or null.
     * @param out       Where to write the Messenger.
     */
    public static void writeMessengerOrNullToParcel(RaCustomMessenger messenger,
                                                    Parcel out) {
        out.writeStrongBinder(messenger != null ? messenger.mTarget.asBinder()
                : null);
    }

    /**
     * Convenience function for reading either a Messenger or null pointer from
     * a Parcel.  You must have previously written the Messenger with
     * {@link #writeMessengerOrNullToParcel}.
     *
     * @param in The Parcel containing the written Messenger.
     * @return Returns the Messenger read from the Parcel, or null if null had
     * been written.
     */
    public static RaCustomMessenger readMessengerOrNullFromParcel(Parcel in) {
        IBinder b = in.readStrongBinder();
        return b != null ? new RaCustomMessenger(b) : null;
    }

    /**
     * Create a Messenger from a raw IBinder, which had previously been
     * retrieved with {@link #getBinder}.
     *
     * @param target The IBinder this Messenger should communicate with.
     */
    public RaCustomMessenger(@Nullable IBinder target) {
        mTarget = target != null ? IRaMessenger.Stub.asInterface(target) : null;
    }
}
