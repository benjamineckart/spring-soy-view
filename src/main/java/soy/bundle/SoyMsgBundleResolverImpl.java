package soy.bundle;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.template.soy.msgs.SoyMsgBundle;
import com.google.template.soy.msgs.SoyMsgBundleHandler;
import com.google.template.soy.msgs.restricted.SoyMsg;
import com.google.template.soy.msgs.restricted.SoyMsgBundleImpl;
import com.google.template.soy.xliffmsgplugin.XliffMsgPlugin;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: mati
 * Date: 20/06/2013
 * Time: 00:01
 */
public class SoyMsgBundleResolverImpl implements SoyMsgBundleResolver {

    private final String DEF_MESSAGES_PATH = "xliffs/messages_";

    private static final Map<Locale, SoyMsgBundle> msgBundles = new ConcurrentHashMap<Locale, SoyMsgBundle>();

    private String messagesPath = DEF_MESSAGES_PATH;

    public SoyMsgBundle resolve(final Locale locale) throws IOException {
        SoyMsgBundle soyMsgBundle = msgBundles.get(locale);
        if (soyMsgBundle == null) {
            soyMsgBundle = createSoyMsgBundle(locale);
            if (soyMsgBundle == null) {
                soyMsgBundle = createSoyMsgBundle(new Locale(locale.getLanguage()));
            }

            if (soyMsgBundle == null) {
                soyMsgBundle = createSoyMsgBundle(Locale.ENGLISH);
            }

            if (soyMsgBundle == null) {
                throw new IOException("No message bundle found.");
            }

            msgBundles.put(locale, soyMsgBundle);
        }

        return soyMsgBundle;
    }

    protected SoyMsgBundle createSoyMsgBundle(final Locale locale) throws IOException {
        final String path = messagesPath + locale.toString() + ".xlf";

        final Enumeration<URL> e = Thread.currentThread().getContextClassLoader().getResources(path);

        final List<SoyMsgBundle> msgBundles = Lists.newArrayList();

        final SoyMsgBundleHandler msgBundleHandler = new SoyMsgBundleHandler(new XliffMsgPlugin());

        while (e.hasMoreElements()) {
            final URL msgFile = e.nextElement();
            msgBundles.add(msgBundleHandler.createFromResource(msgFile));
        }

        return mergeMsgBundles(msgBundles).orNull();
    }

    private Optional<? extends SoyMsgBundle> mergeMsgBundles(final List<SoyMsgBundle> soyMsgBundles) {
        if (soyMsgBundles.isEmpty()) {
            return Optional.absent();
        }

        String localeStr = "";

        final List<SoyMsg> msgs = Lists.newArrayList();

        for (final SoyMsgBundle smb : soyMsgBundles) {
            localeStr = smb.getLocaleString(); //we assume locale is the same

            for (Iterator<SoyMsg> it = smb.iterator(); it.hasNext();) {
                msgs.add(it.next());
            }
        }

        return Optional.of(new SoyMsgBundleImpl(localeStr, msgs));
    }

    public void setMessagesPath(final String messagesPath) {
        this.messagesPath = messagesPath;
    }

}
