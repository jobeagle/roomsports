import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * MapPanel display tiles from openstreetmap as is. This simple minimal viewer supports zoom around mouse-click center and has a simple api.
 * A number of tiles are cached. See {@link #CACHE_SIZE} constant. If you use this it will create traffic on the tileserver you are
 * using. Please be conscious about this.
 *
 * This class is a JPanel which can be integrated into any swing app just by creating an instance and adding like a JLabel.
 *
 * The map has the size <code>256*1<<zoomlevel</code>. This measure is referred to as map-coordinates. Geometric locations
 * like longitude and latitude can be obtained by helper methods. Note that a point in map-coordinates corresponds to a given
 * geometric position but also depending on the current zoom level.
 *
 * You can zoomIn around current mouse position by left double click. Left right click zooms out.
 *
 * 
 * Methods of interest are
 * 
 * {@link #setZoom(int)} which sets the map's zoom level. Values between 1 and 18 are allowed.
 * {@link #setMapPosition(Point)} which sets the map's top left corner. (In map coordinates)
 * {@link #setCenterPosition(Point)} which sets the map's center position. (In map coordinates)
 * for the given longitude and latitude. If you want to center the map around this geometric location you need
 * to pass the result to the method</li>
 * 
 * As mentioned above Longitude/Latitude functionality is available via the method {@link #computePosition(java.awt.geom.Point2D.Double)}.
 * If you have a GIS database you can get this info out of it for a given town/location, invoke {@link #computePosition(java.awt.geom.Point2D.Double)} to
 * translate to a position for the given zoom level and center the view around this position using {@link #setCenterPosition(Point)}.
 * 
 *
 * The properties <code>zoom</code> and <code>mapPosition</code> are bound and can be tracked via
 * regular {@link PropertyChangeListener}s.
 *
 * License is EPL (Eclipse Public License) http://www.eclipse.org/legal/epl-v10.html.  Contact at stepan.rutz@gmx.de
 * Originalsoftware: mappanel (https://github.com/srutz/mappanel)
 *
 * @author stepan.rutz, Erweiterungen: Bruno Schmidt
 * @version $Revision: 1.6 $
 */
public class OSMViewer extends Canvas {
    
    public static class PointD {
        public double x, y;
        public PointD(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    
    public static class PointDExt extends PointD {
        public String name;
        public PointDExt(double x, double y, String name) {
            super(x,y);
            this.name = name;
        }
    }

    public static class TileServer {
        private String url;
        private int maxZoom;
        private boolean broken;
        private boolean switchXY;
        private String extImage;
        private String copyright;

        public TileServer(String url, int maxZoom, boolean switchXY, String extImage, String copyright) {
            this.url = url;
            this.maxZoom = maxZoom;
            this.switchXY = switchXY;
            this.extImage = extImage;
            this.copyright = copyright;
        }
        public String toString() {
            return url;
        }
        public int getMaxZoom() {
            return maxZoom;
        }
        public String getURL() {
            return url;
        }
        public void setURL(String url) {
            this.url = url;
        }
        public boolean isBroken() {
            return broken;
        }
        public void setBroken(boolean broken) {
            this.broken = broken;
        }       
        public boolean isswitchXY() {
            return switchXY;
        }
        public void setswitchXY(boolean switchXY) {
        	this.switchXY = switchXY;
        }
        public String getextImage() {
        	return extImage;
        }
        public void setextImage(String extImage) {
        	this.extImage = extImage;
        }
        public String getCopyright() {
        	return copyright;
        }
        public void setCopyright(String copyright) {
        	this.copyright = copyright;
        }
    }
    
    public static class Stats {
        public int tileCount;
        public long dt;
        private Stats() {
            reset();
        }
        private void reset() {
            tileCount = 0;
            dt = 0;
        }
    }
    
    private static class Tile {
        private final String key;
        public final int x, y, z;
        public Tile(String tileServer, int x, int y, int z) {
            this.key = tileServer;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + x;
            result = prime * result + y;
            result = prime * result + z;
            return result;
        }
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Tile other = (Tile) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (x != other.x)
                return false;
            if (y != other.y)
                return false;
            if (z != other.z)
                return false;
            return true;
        }
    }
    
    public class TileCache {
        private LinkedHashMap<Tile,AsyncImage> map = new LinkedHashMap<Tile,AsyncImage>(CACHE_SIZE, 0.75f, true) {
			private static final long serialVersionUID = -4488L;

			protected boolean removeEldestEntry(Map.Entry<Tile,AsyncImage> eldest) {
                boolean remove = size() > CACHE_SIZE;
                if (remove)
                    eldest.getValue().dispose(getDisplay());
                return remove;
            }
        };
        public void put(TileServer tileServer, int x, int y, int z, AsyncImage image) {
            map.put(new Tile(tileServer.getURL(), x, y, z), image);
        }
        public AsyncImage get(TileServer tileServer, int x, int y, int z) {
            return map.get(new Tile(tileServer.getURL(), x, y, z));
        }
        public void remove(TileServer tileServer, int x, int y, int z) {
            map.remove(new Tile(tileServer.getURL(), x, y, z));
        }
        public int getSize() {
            return map.size();
        }
    }
    
    public final class AsyncImage implements Runnable {
        private final AtomicReference<ImageData> imageData = new AtomicReference<ImageData>();
        private Image image; // might as well be thread-local
        private FutureTask<Boolean> task;
        private volatile long stamp = zoomStamp.longValue();
        private final TileServer tileServer;
        private final int x, y, z;

        public AsyncImage(TileServer tileServer, int x, int y, int z) {
            this.tileServer = tileServer;
            this.x = x;
            this.y = y;
            this.z = z;
            task = new FutureTask<Boolean>(this, Boolean.TRUE);
            executor.execute(task);
        }
        
        public void run() {
        	boolean bloadfail = false;
            String url = getTileString(tileServer, x, y, z);
            if (stamp != zoomStamp.longValue()) {
                try {
                    // here is a race, we just live with.
                    if (!getDisplay().isDisposed()) {
                        getDisplay().asyncExec(new Runnable() {
                            public void run() {
                                getCache().remove(tileServer, x, y, z);
                            }
                        });
                    }
                } catch (SWTException e) {
                    Mlog.info("swt exception bei remove 1");
                }
                
                return;
            }
            try {
                InputStream in = new URL(url).openConnection().getInputStream();
                imageData.set(new ImageData(in));
                try {
                    if (!getDisplay().isDisposed()) {
                        getDisplay().asyncExec(new Runnable() {
                            public void run() {
                                redraw();
                            }
                        });
                    }
                } catch (SWTException e) {
                    Mlog.info("swt exception bei redraw");
                }
            } catch (Exception e) {
                Mlog.debug("failed to load imagedata from url: " + url);
                bloadfail = true;
            }
            // Kachel löschen, bei Ladefehler:
            if (bloadfail) {
                try {
                    if (!getDisplay().isDisposed()) {
                        getDisplay().asyncExec(new Runnable() {
                            public void run() {
                                getCache().remove(tileServer, x, y, z);
                            }
                        });
                    }
                } catch (SWTException e) {
                    Mlog.info("swt exception bei remove 2");
                }            	
            }
        }

        public Image getImage(Display display) {
            checkThread(display);
            if (image == null && imageData.get() != null) {
                image = new Image(display, imageData.get());
            }
            return image;
        }
        
        public void dispose(Display display) {
            checkThread(display);
            if (image != null) {
                image.dispose();
            }
        }
        
        private void checkThread(Display display) {
            // jdk 1.6 bug from checkWidget still fails here
            if (display.getThread() != Thread.currentThread()) {
                throw new IllegalStateException("wrong thread to pick up the image");
            }
        }
    }

    private class MapMouseListener implements MouseListener, MouseWheelListener, MouseMoveListener, MouseTrackListener {
        private Point mouseCoords = new Point(0, 0);
        private Point downCoords;
        private Point downPosition;
        
        public void mouseEnter(MouseEvent e) {
            OSMViewer.this.forceFocus();
        }
        
        public void mouseExit(MouseEvent e) {
        }

        public void mouseHover(MouseEvent e) {
        }
        
        public void mouseDoubleClick(MouseEvent e) {
            if (e.button == 1) 
                zoomIn(new Point(mouseCoords.x, mouseCoords.y));
            else if (e.button == 3)
                zoomOut(new Point(mouseCoords.x, mouseCoords.y));
        }
        public void mouseDown(MouseEvent e) {
            if (e.button == 1 && (e.stateMask & SWT.CTRL) != 0) {
                setCenterPosition(getCursorPosition());
                redraw();
            }
            if (e.button == 1) {
                downCoords = new Point(e.x, e.y);
                downPosition = getMapPosition();
            }
        }
        public void mouseUp(MouseEvent e) {
            if (e.count == 1) {
                handleDrag(e);
            }
            downCoords = null;
            downPosition = null;
        }
        
        public void mouseMove(MouseEvent e) {
            handlePosition(e);
            handleDrag(e);
        }
        public void mouseScrolled(MouseEvent e) {
            if (e.count == 1)
                zoomIn(new Point(mouseCoords.x, mouseCoords.y));
            else if (e.count == -1)
                zoomOut(new Point(mouseCoords.x, mouseCoords.y));
        }
        
        private void handlePosition(MouseEvent e) {
            mouseCoords = new Point(e.x, e.y);
        }

        private void handleDrag(MouseEvent e) {
            if (downCoords != null) {
                int tx = downCoords.x - e.x;
                int ty = downCoords.y - e.y;
                setMapPosition(downPosition.x + tx, downPosition.y + ty);
                OSMViewer.this.redraw();
            }
        }    
    }
    
    /**
     * Erzeugung der URL
     * Abhängig vom Tileserver werden die Reihenfolge von X/Y Kachel und Filextension (.png oder .jpg)
     * gebildet
     * @param tileServer   Tileserver
     * @param xtile        X-Pos
     * @param ytile        Y-Pos
     * @param zoom         Zoomfaktor
     * @return URL der benötigten Kachel
     */
    public static String getTileString(TileServer tileServer, int xtile, int ytile, int zoom) {
    	String tileNr;
    	if (tileServer.switchXY)
    		tileNr = ("" + zoom + "/" + ytile + "/" + xtile);
    	else
    		tileNr= ("" + zoom + "/" + xtile + "/" + ytile);

        String url = tileServer.getURL() + tileNr + tileServer.extImage;
        return url;
    }

    /* Ein paar Tileserver vorgeben, unserer als erstes. Der erste kann über die Konfiguration in der Applikation geändert werden. */
    public static TileServer[] TILESERVERS = { 	
       	new TileServer("http://www.mtbsimulator.de/osm/", 18, false, ".png", "© Openstreetmap contributors"),		// DEFAULT!
       	new TileServer("http://www.mtbsimulator.de/osm/", 18, false, ".png", "© Openstreetmap contributors"),
        new TileServer("https://tile.openstreetmap.de/", 18, false, ".png", "© Openstreetmap contributors"),
        new TileServer("http://c.tile.opencyclemap.org/cycle/", 18, false, ".png", "© Openstreetmap contributors"),
        new TileServer("http://tile.thunderforest.com/outdoors/", 18, false, ".png", "Maps © Thunderforest, Data © OpenStreetMap contributors"),
    };
 
    private static final int TILE_SIZE = 256;
    public static final int CACHE_SIZE = 256;
    public static final int IMAGEFETCHER_THREADS = 4;
    
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Point mapSize = new Point(0, 0);
    private Point mapPosition = new Point(0, 0);
    private int zoom;
    private static final int startzoom = 13;					// Startzoom
    private int posimgalpha = 250;								// Transparenz der angezeigten Images (Pos, Flaggen)
    private AtomicLong zoomStamp = new AtomicLong();

    public static TileServer tileServer = TILESERVERS[0];
    private TileCache cache = new TileCache();
    private Stats stats = new Stats();
    private MapMouseListener mouseListener = new MapMouseListener();

    private List<TrkPt> aktTrack;
    private ArrayList<PointDExt> aktPos = new ArrayList<PointDExt>();
    
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
    private ThreadFactory threadFactory = new ThreadFactory( ) {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("Async Image Loader " + t.getId() + " " + System.identityHashCode(t));
            t.setDaemon(true);
            return t;
        }
    };
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(IMAGEFETCHER_THREADS, 16, 2, TimeUnit.SECONDS, workQueue, threadFactory);
    
    private Color waitBackground, waitForeground;
    
    private boolean zeigePunkte = false;
    
    /**
     * Hier ist der normale Einstieg (Konstruktor)
     * @param parent	Composite
     * @param style		Stil
     * @param track		Track
     */
    public OSMViewer(Composite parent, int style, List<TrkPt> track) {
        this(parent, style, startzoom, track);
    }
    
    public OSMViewer(Composite parent, int style, int zoom, List<TrkPt> track) {
        super(parent, SWT.DOUBLE_BUFFERED | style);
        //centerimage = img;
        setAktTrack(track);
        
        Mlog.info("OSM-Init, zoom: "+zoom);
        waitBackground = new Color(getDisplay(), 0x77, 0x77, 0x77);
        waitForeground = new Color(getDisplay(), 0x77, 0x77, 0x77);
        
        setZoom(zoom);
        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
               OSMViewer.this.widgetDisposed(e);
            }
        });
        addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                OSMViewer.this.paintControl(e);
            }
        });
    }
    
    protected void paintControl(PaintEvent e) {
        GC gc = e.gc;
        
        getStats().reset();
        long t0 = System.currentTimeMillis();
        Point size = getSize();
        //Mlog.debug("OSM-Size: " + size);
        int width = size.x, height = size.y;
        int x0 = (int) Math.floor(((double) mapPosition.x) / TILE_SIZE);
        int y0 = (int) Math.floor(((double) mapPosition.y) / TILE_SIZE);
        int x1 = (int) Math.ceil(((double) mapPosition.x + width) / TILE_SIZE);
        int y1 = (int) Math.ceil(((double) mapPosition.y + height) / TILE_SIZE);

        int dy = y0 * TILE_SIZE - mapPosition.y;
        for (int y = y0; y < y1; ++y) {
            int dx = x0 * TILE_SIZE - mapPosition.x;
            for (int x = x0; x < x1; ++x) {
                paintTile(gc, dx, dy, x, y);
                dx += TILE_SIZE;
                ++getStats().tileCount;
            }
            dy += TILE_SIZE;
        }
        
        showCopyright(gc, 150);
        showTileserver(gc, 120);
        if (aktTrack != null)
        	drawTrack(gc, aktTrack);
        if (aktPos.size() > 0)
        	for (int i=0; i<aktPos.size();i++) {
        		//Mlog.debug("Anzeige Index: "+i+" Name: "+aktPos.get(i).name);
        		showGegPosImage(gc, Global.gegimage, (PointDExt) aktPos.get(i));
        	}
        // akt. Position ist in Bildmitte, Mit RS-Icon markieren!
        showActPosImage(gc, Global.posimage);
        
        long t1 = System.currentTimeMillis();
        stats.dt = t1 - t0;
    }
    
    private void paintTile(GC gc, int dx, int dy, int x, int y) {
        Display display = getDisplay();
        boolean DRAW_IMAGES = true;
        boolean DEBUG = false;
        boolean DRAW_OUT_OF_BOUNDS = true;

        boolean imageDrawn = false;
        int xTileCount = 1 << zoom;
        int yTileCount = 1 << zoom;
        boolean tileInBounds = x >= 0 && x < xTileCount && y >= 0 && y < yTileCount;
        boolean drawImage = DRAW_IMAGES && tileInBounds;
        if (drawImage) {
            TileCache cache = getCache();
            TileServer tileServer = getTileServer();
            AsyncImage image = cache.get(tileServer, x, y, zoom);
            if (image == null) {
                image = new AsyncImage(tileServer, x, y, zoom);
                cache.put(tileServer, x, y, zoom, image);
            }
            if (image.getImage(getDisplay()) != null) {
                gc.drawImage(image.getImage(getDisplay()), dx, dy);
                imageDrawn = true;
            }
        }
        if (DEBUG && (!imageDrawn && (tileInBounds || DRAW_OUT_OF_BOUNDS))) {
            gc.setBackground(display.getSystemColor(tileInBounds ? SWT.COLOR_GREEN : SWT.COLOR_RED));
            gc.fillRectangle(dx + 4, dy + 4, TILE_SIZE - 8, TILE_SIZE - 8);
            gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
            String s = "T " + x + ", " + y + (!tileInBounds ? " #" : "");
            gc.drawString(s, dx + 4+ 8, dy + 4 + 12);
        }  else if (!DEBUG && !imageDrawn && tileInBounds) {
            gc.setBackground(waitBackground);
            gc.fillRectangle(dx, dy, TILE_SIZE, TILE_SIZE);
            gc.setForeground(waitForeground);
            for (int yl = 0; yl < TILE_SIZE; yl += 32) {
            	gc.drawLine(dx, dy + yl, dx + TILE_SIZE, dy + yl);
            }
            for (int xl = 0; xl < TILE_SIZE; xl += 32) {
            	gc.drawLine(dx + xl, dy, dx + xl, dy + TILE_SIZE);
            }
        }
    }

    protected void widgetDisposed(DisposeEvent e) {
        waitBackground.dispose();
        waitForeground.dispose();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }
    
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }
    
    public TileCache getCache() {
        return cache;
    }
    
    public TileServer getTileServer() {
        return tileServer;
    }
    
    public void setTileServer(TileServer tileServer) {
        OSMViewer.tileServer = tileServer;
        redraw();
    }
    
    public Stats getStats() {
        return stats;
    }
    
    public Point getMapPosition() {
        return new Point(mapPosition.x, mapPosition.y);
    }

    public void setMapPosition(Point mapPosition) {
        setMapPosition(mapPosition.x, mapPosition.y);
    }

    public void setMapPosition(int x, int y) {
        if (mapPosition.x == x && mapPosition.y == y)
            return;
        Point oldMapPosition = getMapPosition();
        mapPosition.x = x;
        mapPosition.y = y;
        pcs.firePropertyChange("mapPosition", oldMapPosition, getMapPosition());
    }
    
    public void translateMapPosition(int tx, int ty) {
        setMapPosition(mapPosition.x + tx, mapPosition.y + ty);
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        if (zoom == this.zoom)
            return;
        zoomStamp.incrementAndGet();
        int oldZoom = this.zoom;
        this.zoom = Math.min(getTileServer().getMaxZoom(), zoom);
        mapSize.x = getXMax();
        mapSize.y = getYMax();
        pcs.firePropertyChange("zoom", oldZoom, zoom);
    }

    public void zoomIn(Point pivot) {
        if (getZoom() >= getTileServer().getMaxZoom())
            return;
        Point mapPosition = getMapPosition();
        int dx = pivot.x;
        int dy = pivot.y;
        setZoom(getZoom() + 1);
        setMapPosition(mapPosition.x * 2 + dx, mapPosition.y * 2 + dy);
        redraw();
    }

    public void zoomOut(Point pivot) {
        if (getZoom() <= 1)
            return;
        Point mapPosition = getMapPosition();
        int dx = pivot.x;
        int dy = pivot.y;
        setZoom(getZoom() - 1);
        setMapPosition((mapPosition.x - dx) / 2, (mapPosition.y - dy) / 2);
        redraw();
    }

    public int getXTileCount() {
        return (1 << zoom);
    }

    public int getYTileCount() {
        return (1 << zoom);
    }

    public int getXMax() {
        return TILE_SIZE * getXTileCount();
    }

    public int getYMax() {
        return TILE_SIZE * getYTileCount();
    }
    public Point getCursorPosition() {
        return new Point(mapPosition.x + mouseListener.mouseCoords.x, mapPosition.y + mouseListener.mouseCoords.y);
    }
    public Point getTile(Point position) {
        return new Point((int) Math.floor(((double) position.x) / TILE_SIZE),(int) Math.floor(((double) position.y) / TILE_SIZE));
    }

    public Point getCenterPosition() {
        org.eclipse.swt.graphics.Point size = getSize();
        return new Point(mapPosition.x + size.x / 2, mapPosition.y + size.y / 2);
    }

    public void setCenterPosition(Point p) {
        org.eclipse.swt.graphics.Point size = getSize();
        setMapPosition(p.x - size.x / 2, p.y - size.y / 2);
    }

    public PointD getLongitudeLatitude(Point position) {
        return new PointD(
                position2lon(position.x, getZoom()),
                position2lat(position.y, getZoom()));
    }

    /**
	 * @return zeigePunkte
	 */
	public boolean isZeigePunkte() {
		return zeigePunkte;
	}

	/**
	 * @param zeigePunkte zeigePunkte ein oder aus
	 */
	public void setZeigePunkte(boolean zeigePunkte) {
		this.zeigePunkte = zeigePunkte;
	}

	public Point computePosition(PointD coords) {
        int x = lon2position(coords.x, getZoom());
        int y = lat2position(coords.y, getZoom());
        return new Point(x, y);
    }

    /**
     * Position des Gegners in ArrayList aktPos speichern. Der übergebene Index startet bei 0 und
     * wenn die Größe der Arraylist nicht ausreicht wird vergrößert.
     * @param index Index
     * @param lon   Longitude
     * @param lat   Latitude
     * @param name  Name des Gegners
     */
    public void setPosition(int index, double lon, double lat, String name) {
    	//Mlog.debug("setPosition index: "+index+" - size: "+aktPos.size() + " - Name: " + name);
    	PointDExt pos = new PointDExt(lon, lat, name);
    	if (index >= aktPos.size())
    		aktPos.add(pos);
    	else
    		aktPos.set(index, pos);
    }
    
    /**
     * Anzeige der aktuellen Position in der Mitte der Karte.
     * @param gc  gc
     * @param img Image
     */
    public void showActPosImage(GC gc, Image img) {
        org.eclipse.swt.graphics.Point size = getSize();
        gc.drawImage(img, size.x/2 - img.getBounds().width/2, size.y/2 - img.getBounds().height);
    }

    /**
     * Anzeige der gegnerischen Position auf der Karte.
     * @param gc   gc
     * @param img  Image
     * @param pos  Position
     */
    public void showGegPosImage(GC gc, Image img, PointDExt pos) {
        Point gegPt = computePosition(pos);
        Font font = new Font(gc.getDevice(),"Arial",7,0);
        gc.setFont(font);
        gc.drawImage(img, -(mapPosition.x-gegPt.x) - img.getBounds().width/2, -(mapPosition.y-gegPt.y));
        gc.drawText(pos.name+" ", -(mapPosition.x-gegPt.x)+img.getBounds().width/2+2, -(mapPosition.y-gegPt.y) + img.getBounds().height/2);
    }

    /**
     * Copyright Text anzeigen
     * @param gc      gc
     * @param alpha   Transparenzfaktor
     */
    public void showCopyright(GC gc, int alpha) {
        Font font = new Font(gc.getDevice(),"Arial",8,0);
        gc.setFont(font);
        org.eclipse.swt.graphics.Point size = getSize();
        gc.setAlpha(alpha);
        gc.drawText(tileServer.getCopyright(), 4, size.y-15);
    }

    /**
     * Tileserver-String anzeigen
     * @param gc     gc
     * @param alpha  Transparenzfaktor
     */
    public void showTileserver(GC gc, int alpha) {
        Font font = new Font(gc.getDevice(),"Arial",8,0);
        gc.setFont(font);
        gc.setAlpha(alpha);
        gc.drawText("Tileserver: "+tileServer.getURL(), 4, 2);
    }

    /**
     * Track als Linie zeichnen.
     * @param gc    gc
     * @param track Track
     */
    public void drawTrack(GC gc, List<TrkPt> track) {
    	int i = -1;
		Iterator<TrkPt> it = track.iterator();
		Point lastPt = new Point(0, 0);
        Point mapPosition = getMapPosition();
        gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
        gc.setLineWidth(3);
        gc.setAlpha(posimgalpha);

        // Startpunkt markieren:
		PointD coords = new PointD(track.get(0).getLongitude(), track.get(0).getLatitude());
		Point pt = computePosition(coords);
        gc.drawImage(Global.startimage, -(mapPosition.x-pt.x) -3, -(mapPosition.y-pt.y) - Global.startimage.getBounds().height);
        
		while(it.hasNext()) {
			i++;
			// Position berechnen und zeichnen
			TrkPt aktpoint = (TrkPt) it.next(); 
			coords = new PointD(aktpoint.getLongitude(), aktpoint.getLatitude());
			pt = computePosition(coords);
			if (isZeigePunkte()) {
				gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
				gc.drawRectangle(-(mapPosition.x-pt.x+2), -(mapPosition.y-pt.y+2), 5, 5);
				gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
			}
			if (lastPt.x != 0 && lastPt.y != 0)
				gc.drawLine(-(mapPosition.x-lastPt.x), -(mapPosition.y-lastPt.y), -(mapPosition.x-pt.x), -(mapPosition.y-pt.y));
			lastPt = pt;
		}
        // Zielpunkt markieren:
		coords = new PointD(track.get(i).getLongitude(), track.get(i).getLatitude());
		pt = computePosition(coords);
        gc.drawImage(Global.zielimage, -(mapPosition.x-pt.x) - 3, -(mapPosition.y-pt.y) - Global.zielimage.getBounds().height);
    }
    
    /**
     * Setzt einen neuen Track zur Darstellung
     * @param track  Trackliste
     */
    public void setAktTrack(List<TrkPt> track) {
        aktTrack = track;
    }
    
    //-------------------------------------------------------------------------
    // utils
    public static String format(double d) {
        return String.format("%.5f", d);
    }

    public static double getN(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return n;
    }

    public static double position2lon(int x, int z) {
        double xmax = TILE_SIZE * (1 << z);
        return x / xmax * 360.0 - 180;
    }

    public static double position2lat(int y, int z) {
        double ymax = TILE_SIZE * (1 << z);
        return Math.toDegrees(Math.atan(Math.sinh(Math.PI - (2.0 * Math.PI * y) / ymax)));
    }

    public static double tile2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    public static double tile2lat(int y, int z) {
        return Math.toDegrees(Math.atan(Math.sinh(Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z))));
    }

    public static int lon2position(double lon, int z) {
        double xmax = TILE_SIZE * (1 << z);
        return (int) Math.floor((lon + 180) / 360 * xmax);
    }

    public static int lat2position(double lat, int z) {
        double ymax = TILE_SIZE * (1 << z);
        return (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * ymax);
    }

    public static String getTileNumber(TileServer tileServer, double lat, double lon, int zoom) {
        int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
        int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
        return getTileString(tileServer, xtile, ytile, zoom);
    }    
    
    public final class MapBrowserComposite extends Composite {
        private SashForm sashForm;

        public MapBrowserComposite(Composite parent, int style) {
            super(parent, style);
            setLayout(new FillLayout());            
            sashForm = new SashForm(this, SWT.HORIZONTAL);
            sashForm.setLayout(new FillLayout());            
        }
    }
}


