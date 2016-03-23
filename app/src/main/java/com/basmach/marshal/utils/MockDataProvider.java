package com.basmach.marshal.utils;

import android.content.Context;
import android.util.Log;

import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.entities.MaterialItem;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;

import java.util.ArrayList;
import java.util.List;

public class MockDataProvider {

    private final Context mContext;

    private final String[] IMAGES = {"http://cdn2.hubspot.net/hubfs/206683/cyber-security-training.jpg?t%5Cu003d1430137590751",
            "https://www.dunebook.com/wp-content/uploads/2015/07/angular-dunebook.png",
            "http://www.wingnity.com/uploads/Courses/1396070428_android-course.png",
            "https://academy.mymagic.my/app/uploads/2015/08/FRONTEND_ma-01-700x400-c-default.jpg",
            "https://udemy-images.udemy.com/course/750x422/352132_74cf_2.jpg"};

    public MockDataProvider(Context context) {
        this.mContext = context;
    }

    public void insertAllCycles() {
        ArrayList<Cycle> items = getAllCycles();
        for (Cycle item:items) {
            try {
                item.create();
                Log.i("INSERT CYCLE ", String.valueOf(item.getId()));
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("INSERT CYCLE ",e.getMessage());
            }
        }
    }

    public void insertAllCourses() {
        ArrayList<Course> items = getAllCourses();
        for (Course item:items) {
            try {
                item.createInBackground(mContext, false, new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {
                        Log.i("INSERT COURSE ","success");
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("INSERT COURSE ","failed");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("INSERT COURSE ",e.getMessage());
            }
        }
    }

    public void insertAllMaterialItems() {
        ArrayList<MaterialItem> items = getAllMaterials();
        for (MaterialItem item:items) {
            try {
                item.createInBackground(mContext, false, new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {
                        Log.i("INSERT MATERIAL ITEM ","success");
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("INSERT MATERIAL ITEM ","failed");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("INSERT ALL MATERIALS ",e.getMessage());
            }
        }
    }

    private ArrayList<Cycle> getAllCycles() {
        ArrayList<Cycle> tempList = new ArrayList<>();

        Cycle cyberCycle1 = new Cycle(mContext);
        cyberCycle1.setStartDate(DateHelper.stringToDate("23/06/16"));
        cyberCycle1.setEndDate(DateHelper.stringToDate("01/07/16"));
        cyberCycle1.setName("מתודולוגיות פיתוח מאובטח");
        cyberCycle1.setDescription("פיתוח מערכות הוא אחת מנקודות הקשר המעניינות ביותר בין עולם הפיתוח לעולם אבטחת המידע. בעוד שאנשי הפיתוח ניצבים מול אתרים טכנולוגיים מורכבים, לו\"ז קשוח ועומס עבודה טבעי, הם נדרשים גם לעמוד לא פעם בפני אתגרים הנוגעים להיבטי אבטחת המידע של המערכת. פיתוח ההשתלמות החל במטרה לספק הכשרה רחבה יותר ומקיפה על יישום תכונות אבטחה ברמות שונות. ההשתלמות מכשירה להבנת האיומים על מערכת באמצעות ניתוח מודל איומים וזיהוי כשלי אבטחה עוד לפני כתיבת הקוד. בנוסף, נושא הגדרת מדיניות מאובטחת בארגון ואכיפתה נכנס במטרה להטמיע את אופן החשיבה המאובטח והצבת תשתית תפיסתית לפיה יעבדו התכניתנים.");
        tempList.add(cyberCycle1);
        Cycle cyberCycle2 = new Cycle(mContext);
        cyberCycle2.setStartDate(DateHelper.stringToDate("05/09/16"));
        cyberCycle2.setEndDate(DateHelper.stringToDate("10/09/16"));
        cyberCycle2.setName("מתודולוגיות פיתוח מאובטח");
        cyberCycle2.setDescription("פיתוח מערכות הוא אחת מנקודות הקשר המעניינות ביותר בין עולם הפיתוח לעולם אבטחת המידע. בעוד שאנשי הפיתוח ניצבים מול אתרים טכנולוגיים מורכבים, לו\"ז קשוח ועומס עבודה טבעי, הם נדרשים גם לעמוד לא פעם בפני אתגרים הנוגעים להיבטי אבטחת המידע של המערכת. פיתוח ההשתלמות החל במטרה לספק הכשרה רחבה יותר ומקיפה על יישום תכונות אבטחה ברמות שונות. ההשתלמות מכשירה להבנת האיומים על מערכת באמצעות ניתוח מודל איומים וזיהוי כשלי אבטחה עוד לפני כתיבת הקוד. בנוסף, נושא הגדרת מדיניות מאובטחת בארגון ואכיפתה נכנס במטרה להטמיע את אופן החשיבה המאובטח והצבת תשתית תפיסתית לפיה יעבדו התכניתנים.");
        tempList.add(cyberCycle2);
        Cycle cyberCycle3 = new Cycle(mContext);
        cyberCycle3.setStartDate(DateHelper.stringToDate("08/05/17"));
        cyberCycle3.setEndDate(DateHelper.stringToDate("13/05/17"));
        cyberCycle3.setName("מתודולוגיות פיתוח מאובטח");
        cyberCycle3.setDescription("פיתוח מערכות הוא אחת מנקודות הקשר המעניינות ביותר בין עולם הפיתוח לעולם אבטחת המידע. בעוד שאנשי הפיתוח ניצבים מול אתרים טכנולוגיים מורכבים, לו\"ז קשוח ועומס עבודה טבעי, הם נדרשים גם לעמוד לא פעם בפני אתגרים הנוגעים להיבטי אבטחת המידע של המערכת. פיתוח ההשתלמות החל במטרה לספק הכשרה רחבה יותר ומקיפה על יישום תכונות אבטחה ברמות שונות. ההשתלמות מכשירה להבנת האיומים על מערכת באמצעות ניתוח מודל איומים וזיהוי כשלי אבטחה עוד לפני כתיבת הקוד. בנוסף, נושא הגדרת מדיניות מאובטחת בארגון ואכיפתה נכנס במטרה להטמיע את אופן החשיבה המאובטח והצבת תשתית תפיסתית לפיה יעבדו התכניתנים.");
        tempList.add(cyberCycle3);

        return tempList;
    }
    private ArrayList<Course> getAllCourses() {
        ArrayList<Course> tempList = new ArrayList<>();

        Course courseCyber = new Course(mContext);
        courseCyber.setImageUrl(IMAGES[0]);
        courseCyber.setName("מתודולוגיות פיתוח מאובטח");
        courseCyber.setCourseCode("0440");
        courseCyber.setDescription("פיתוח מערכות הוא אחת מנקודות הקשר המעניינות ביותר בין עולם הפיתוח לעולם אבטחת המידע. בעוד שאנשי הפיתוח ניצבים מול אתרים טכנולוגיים מורכבים, לו\"ז קשוח ועומס עבודה טבעי, הם נדרשים גם לעמוד לא פעם בפני אתגרים הנוגעים להיבטי אבטחת המידע של המערכת. פיתוח ההשתלמות החל במטרה לספק הכשרה רחבה יותר ומקיפה על יישום תכונות אבטחה ברמות שונות. ההשתלמות מכשירה להבנת האיומים על מערכת באמצעות ניתוח מודל איומים וזיהוי כשלי אבטחה עוד לפני כתיבת הקוד. בנוסף, נושא הגדרת מדיניות מאובטחת בארגון ואכיפתה נכנס במטרה להטמיע את אופן החשיבה המאובטח והצבת תשתית תפיסתית לפיה יעבדו התכניתנים.");
        courseCyber.setPrerequisites("לחייל יש מקצוע מגן בסייבר או לחייל יש השכלה אקדמאי מוסמך או לחייל יש מקצוע תכניתן וגם ישנה יתרת שירות חצי שנה");
        courseCyber.setTargetPopulation("תוכניתנים או בוגרי מדעי המחשב או בוגרי הנדסה.");
        courseCyber.setSyllabus("* הצורך באפליקציה מאובטחת.\r\n* HTML Related Security Issues.\r\n* Input Validation and Output Sanitation.\r\n* Working with Databases.\r\n* User Authentication & Authorization.\r\n* Code Injection Methods.\r\n* Web Services Security. \r\n* אבטחת סודות.\r\n* Security by Design. \r\n* Secure Design Patterns.\r\n* Advanced Security Mechanisms.\r\n");
        courseCyber.setDurationInDays(5);
        courseCyber.setDurationInHours(40);
        courseCyber.setDayTime("בוקר-צהריים");
        courseCyber.setComments("יש לעבור מבחן כניסה");
        courseCyber.setIsMooc(false);

        for (Cycle cycle:getAllCycles()) {
            courseCyber.addCycle(cycle);
        }

        tempList.add(courseCyber);

        Course courseAngular = new Course(mContext);
        courseAngular.setImageUrl(IMAGES[1]);
        courseAngular.setName("AngularJS");
        courseAngular.setCourseCode("4669");
        courseAngular.setDescription("השתלמות אנגולר רשתית - \r\nמטרתה של השתלמות AngularJS היא להקנות ידע ואת הכלים הנדרשים להתחלת פיתוח בטכנולוגיה זו. AngularJS הינה הפלטפורמה המובילה כיום לפיתוח בתחום ה - WEB. \r\nהכשרה כוללת סקירה כללית של הספריה, יתרונותיה והסבר הארכיטקטורה, קטעי טקסט ווידאו ללמידה עצמית. ומלווה בתרגיל מתגלגל מעשי הנדרש להגשה לאורך ההתקדמות בהשתלמות. \r\nההשתלמות הינה 90 שעות, הפרושים על שלושה שבועות. במהלך שבועות אלו, תידרשו לבצע מטלות ולהגישם בהתאם לתאריך ההגשה המפורסם במערכת השעות הקורסית. בסוף ההשתלמות יתבצע מבחן סיום מעשי בכיתה בבסמ\"ח. ניתן יהיה לגשת לבחינה רק לאחר הגשת כל המטלות ומעבר כל הבחנים בקורס. \r\n-מעבר הקורס מותנה בציון מעבר 70 בכלל הבחנים ומבחן ההסמכה. \r\n-שימו לב כי נדרש להצטייד באוזניות על מנת לשמוע את הסרטונים שיש במהלך הקורס. ");
        courseAngular.setTargetPopulation("תוכניתנים עם רקע בפיתוח WEB.");
        courseAngular.setSyllabus("מבוא לספריית הAngular\r\nData Bindings & Controller\r\nServices\r\nRouting\r\nHTTP\r\nרכיבים נפוצים באנגולר\r\nBasic Directives\r\nAdvanced Directives\r\nAdvanced Scope\r\nPromise\r\nProvider&Forms\r\nמבחן הסמכה");
        courseAngular.setDurationInDays(15);
        courseAngular.setDurationInHours(80);
        courseAngular.setDayTime("בוקר-צהריים");
        courseAngular.setComments("חובה לבצע כניסה ראשונית למערכת \"יוחנן המלמד\" לפני הגשת הבקשה לקורס בכתובת -  https://learn.joe.idf");
        courseAngular.setIsMooc(true);
        tempList.add(courseAngular);

        Course courseAndroid = new Course(mContext);
        courseAndroid.setImageUrl(IMAGES[2]);
        courseAndroid.setName("פיתוח אפליקציות בAndroid");
        courseAndroid.setCourseCode("9777");
        courseAndroid.setDescription("בקורס ילמדו מגוון היבטים הדרושים לפיתוח אפליקציות למכשירים מבוססי Android.");
        courseAndroid.setTargetPopulation("תכניתנים, עתודאים בוגרי תואר ראשון במדמ\"ח / הנדסת תכנה");
        courseAndroid.setSyllabus("מבוא לפיתוח אנדרואיד\r\nממשק משתמש ? מבוא ופקדים בסיסיים\r\nממשק משתמש ? פקדים מתקדמים\r\nממשק משתמש ? פקדים מורכבים\r\nActivities & Fragments\r\nData Storage\r\nBackground Tasks\r\nIntents\r\nSystem Services\r\nLocation-based apps\r\nSensors\r\nApp Release & Google Play");
        courseAndroid.setDurationInDays(5);
        courseAndroid.setDurationInHours(45);
        courseAndroid.setDayTime("בוקר-צהריים");
        courseAndroid.setIsMooc(false);
        tempList.add(courseAndroid);

        Course courseFrontend = new Course(mContext);
        courseFrontend.setImageUrl(IMAGES[3]);
        courseFrontend.setName("פיתוח Front-end");
        courseFrontend.setCourseCode("2633");
        courseFrontend.setDescription("עקרונות בניית אפליקציות WEB דינאמיות תוך שימוש ביכולות השונות של השפות ? HTML ו Javascript וספריות JQuery וBootStrap.");
        courseFrontend.setTargetPopulation("תכניתנים, מגן בסייבר, כותב לומדה, נתמ\"מ");
        courseFrontend.setSyllabus("מבוא ל-HTML\r\nThe new structure and semantics in HTML5\r\nForms\r\nHTML5 Forms\r\nמבוא ל-CSS\r\nCSS3\r\nמבוא ל-Javascript\r\nAdvanced Javascript\r\njQuery\r\njQuery & AJAX\r\nWeb Storage\r\nWeb Sockets & SSE\r\nWeb Workers\r\nBootstrap\r\nWeb Security\r\nECMAScript6");
        courseFrontend.setDurationInDays(5);
        courseFrontend.setDurationInHours(45);
        courseFrontend.setDayTime("בוקר-צהריים");
        courseFrontend.setIsMooc(false);
        tempList.add(courseFrontend);

        Course coursePhotshop = new Course(mContext);
        coursePhotshop.setImageUrl(IMAGES[4]);
        coursePhotshop.setName("Adobe Photoshop CS4");
        coursePhotshop.setCourseCode("0285");
        coursePhotshop.setDescription("הקניית ידע וכלים בעריכת תמונות בתוכנת Photoshop.");
        coursePhotshop.setTargetPopulation("כלל צה\"ל.");
        coursePhotshop.setSyllabus("מבוא לגרפיקה\r\nמבוא לפוטושופ\r\nציור וצבעים\r\nבחירה ושינוי צורה\r\nהיסטוריה\r\nשכבות\r\nשכפול ותיקון\r\nפילטרים\r\nמסלולים\r\nמסכות\r\nצורות\r\nטקסט\r\nאפקטים וסגנונות\r\nעצמים חכמים\r\nעריכת צבע\r\nאנימציה\r\nאוטומציה");
        coursePhotshop.setDurationInDays(5);
        coursePhotshop.setDurationInHours(45);
        coursePhotshop.setDayTime("בוקר-צהריים");
        courseCyber.setComments("אין.");
        coursePhotshop.setIsMooc(false);
        tempList.add(coursePhotshop);

        return tempList;
    }

    private ArrayList<MaterialItem> getAllMaterials() {
        ArrayList<MaterialItem> tempList = new ArrayList<>();

        MaterialItem m1 = new MaterialItem(mContext);
        MaterialItem m2 = new MaterialItem(mContext);
        MaterialItem m3 = new MaterialItem(mContext);
        MaterialItem m4 = new MaterialItem(mContext);
        MaterialItem m5 = new MaterialItem(mContext);
        MaterialItem m6 = new MaterialItem(mContext);
        MaterialItem m7 = new MaterialItem(mContext);

        m1.setUrl("http://stackoverflow.com/questions/28525112/android-recyclerview-vs-listview-with-viewholder");
        m1.setTitle("Android Recyclerview vs ListView with Viewholder");
        m1.setDescription("I recently came across the android RecycleView which was released with Android 5.0 and it seems that RecycleView is just an encapsulated traditional ListView with the ViewHolder pattern incorporate...");
        m1.setCannonicalUrl("stackoverflow.com");
        m1.setImageUrl("http://cdn.sstatic.net/stackoverflow/img/apple-touch-icon@2.png?v=73d79a89bded&a");

        m2.setUrl("https://github.com/LeonardoCardoso/Android-Link-Preview");
        m2.setTitle("LeonardoCardoso/Android-Link-Preview");
        m2.setDescription("Android-Link-Preview - It makes a preview from an url, grabbing all informations. Such as title, relevant texts and images. This a version for Android of my web link preview http://lab.leocardz.com/facebook-link-preview-php--jquery/");
        m2.setCannonicalUrl("github.com");
        m2.setImageUrl("https://avatars0.githubusercontent.com/u/1775157?v=3&s=400");

        m3.setUrl("https://www.youtube.com/watch?v=golN1j4rAug");
        m3.setTitle("How to connect android app to database (JSON) part 1");
        m3.setDescription("Our webSite http://www.mytechhive.com android development Java JSONParser, Json MySQl these our facebook pages : https://www.facebook.com/techapps.official h...");
        m3.setCannonicalUrl("m.youtube.com");
        m3.setImageUrl("https://i.ytimg.com/vi/golN1j4rAug/hqdefault.jpg");

        m4.setUrl("https://www.instagram.com/p/BCpzIvrnjCQ/?taken-by=ido.amram");
        m4.setTitle("Ido Amram on Instagram: “camomile”");
        m4.setDescription("“camomile”");
        m4.setCannonicalUrl("www.instagram.com");
        m4.setImageUrl("https://igcdn-photos-a-a.akamaihd.net/hphotos-ak-xfp1/t51.2885-15/e35/12530818_1560249540952808_430253052_n.jpg?ig_cache_key=MTIwMDcxNTY3Njk5NjQ4MTE2OA%3D%3D.2");

        m5.setUrl("http://www.sport5.co.il/HTML/Articles/Article.398.210717.html");
        m5.setTitle("10 שערים ב-100 שניות");
        m5.setDescription("הערב האחרון של שמינית גמר ליגת האלופות ייכנס להיכל התהילה של הכדורגל. הגאונות של יובנטוס, הטירוף של באיירן והגולאסו של ברצלונה. טסים לרבע בפול גז - צפו בקליפ");
        m5.setCannonicalUrl("www.sport5.co.il");
        m5.setImageUrl("http://www.sport5.co.il/Sip_Storage/FILES/0/size475x318/591500.jpg");

        m6.setUrl("https://www.wetsuitcentre.co.uk/firewire-baked-potato-timber-tek-surfboard-5ft-5.html");
        m6.setTitle("Firewire Baked Potato Timber Tek Surfboard 5ft 5");
        m6.setDescription("This Board Is Built For Catching Waves");
        m6.setCannonicalUrl("www.wetsuitcentre.co.uk");
        m6.setImageUrl("https://wscmediaurl-wetsuitcentre.netdna-ssl.com/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/f/i/firewire-timbertek_2_1.jpg");

        m7.setUrl("http://veg.anonymous.org.il/art4.html");
        m7.setTitle("חלבון - המדריך המלא | אתר הצמחונות והטבעונות הישראלי");
        m7.setDescription("החלבון חשוב לבניית רקמות הגוף ולתפקודן. אך מה מקורות החלבון המומלצים בתזונה? ומה ההבדל בין חלבון מהחי ובין חלבון מהצומח? על כך בכתבה זו.");
        m7.setCannonicalUrl("veg.anonymous.org.il");
        m7.setImageUrl("http://veg.anonymous.org.il/CMS/content/article_thumbnails/4.jpg");

        tempList.add(m1);
        tempList.add(m2);
        tempList.add(m3);
        tempList.add(m4);
        tempList.add(m5);
        tempList.add(m6);
        tempList.add(m7);

        return tempList;
    }
}
