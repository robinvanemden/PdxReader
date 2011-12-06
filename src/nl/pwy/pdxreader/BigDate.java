package nl.pwy.pdxreader;

import java.util.TimeZone;

public class BigDate implements Cloneable, java.io.Serializable {

    /**
     * Constructor for the null date.
     * Gets set to null date, NOT current date.
     * localToday() will create an object initialised to today's date.
     */
    public BigDate() {

    }

    /**
     * Copy constructor
     *
     * @param b an existing nl.pwy.pdxreader.BigDate object to use as a model for cloning another.
     */
    public BigDate(BigDate b) {
        this.ordinal = b.ordinal;
        this.yyyy = b.yyyy;
        this.mm = b.mm;
        this.dd = b.dd;
    }

    /**
     * Ordinal constructor.
     * The ordinal must be NULL_ORDINAL or in the range
     * -365968798 to 364522971
     * i.e. 999,999 BC to 999,999 AD
     *
     * @param ordinal days since 1970 Jan 01.
     */
    private BigDate(int ordinal) {
        // save ordinal field and compute Gregorian equivalent
        set(ordinal);
    } // end nl.pwy.pdxreader.BigDate constructor

    /**
     * Construct a nl.pwy.pdxreader.BigDate object given a Gregorian date yyyy, mm, dd;
     * always rejects invalid dates.
     * A null date is yyyy,mm,dd=0.
     * BEWARE! In Java a lead 0 on an integer implies OCTAL.
     * invalid yyyy mm dd will raise in InvalidArgumentException.
     *
     * @param yyyy -999,999 (BC) to +999,999 (AD)
     * @param mm   month 1 to 12 (not 0 to 11 as in Sun's Date)
     * @param dd   day 1 to 31
     */
    public BigDate(int yyyy, int mm, int dd) {
        set(yyyy, mm, dd, CHECK);
    } // end nl.pwy.pdxreader.BigDate constructor

    /**
     * Construct a nl.pwy.pdxreader.BigDate object given a Gregorian date yyyy, mm, dd;
     * allows control of how invalid dates are handled.
     * A null date is yyyy,mm,dd=0.
     * BEWARE! In Java a lead 0 on an integer implies OCTAL.
     *
     * @param yyyy -999,999 (BC) to +999,999 (AD)
     * @param mm   month 1 to 12 (not 0 to 11 as in Sun's Date)
     * @param dd   day 1 to 31
     * @param how  one of CHECK BYPASSCHECK NORMALIZE NORMALISE
     */
    public BigDate(int yyyy, int mm, int dd, int how) {
        // save yyyy, mm, dd, and compete the ordinal equivalent
        set(yyyy, mm, dd, how);
    } // end nl.pwy.pdxreader.BigDate constructor

    // p u b l i c   m e t h o d s

    /**
     * Determines if this date comes after some other date.
     * Conceptually returns (this - anotherBigDate).
     * compareTo() == 0 is faster than equals().
     *
     * @param anotherBigDate date to compare against
     * @return a positive number if this date > (after) anotherBigDate.<BR>
     *         zero if this date = anotherBigDate.<BR>
     *         a negative number ifd this date < (before) anotherBigDate.
     */
    public final int compareTo(BigDate anotherBigDate) {
        return ordinal - anotherBigDate.getOrdinal();
    }

    /**
     * Compares with another nl.pwy.pdxreader.BigDate to see if they refer to the same date.
     *
     * @param d other nl.pwy.pdxreader.BigDate to compare with this one.
     * @return true if nl.pwy.pdxreader.BigDate d refers to the same date
     */
    public final boolean equals(Object d) {
        return d == this || d instanceof BigDate && (ordinal == ((BigDate) d).getOrdinal());
    }

    /**
     * Multiply then divide using floored rather than the usual truncated
     * arithmetic, using a long intermediate.
     *
     * @param multiplicand one of two numbers to multiply together
     * @param multiplier   one of two numbers to multiply together
     * @param divisor      number to divide by
     * @return (multiplicand x multplier) / divisor
     */
    private static int flooredMulDiv(int multiplicand, int multiplier, int divisor) {
        long result = (long) multiplicand * (long) multiplier;
        if (result >= 0) return (int) (result / divisor);
        else return (int) ((result - divisor + 1) / divisor);
    } // end flooredMulDiv

    /**
     * Get day of week for this nl.pwy.pdxreader.BigDate.
     * Is it zero-based starting with Sunday.
     *
     * @return day of week 0=Sunday to 6=Saturday
     */
    final int getDayOfWeek() {
        // modulus in Java is "broken" for negative numbers
        // so we adjust to make the dividend postive
        return (ordinal == NULL_ORDINAL) ? 0 : ((ordinal + SundayIsZeroAdjustment - MIN_ORDINAL) % 7);
    }

    /**
     * Get day of week 1 to 7 for this nl.pwy.pdxreader.BigDate according to the ISO standard IS-8601.
     * It is one-based starting with Monday.
     * See getDayOfWeek.
     *
     * @return day of week 1=Monday to 7=Sunday, 0 for null date.
     */
    public final int getISODayOfWeek() {
        // modulus in Java is "broken" for negative numbers
        // so we adjust to make the dividend postive
        return (ordinal == NULL_ORDINAL) ? 0 : ((ordinal + MondayIsZeroAdjustment - MIN_ORDINAL) % 7) + 1;
    }

    /**
     * Get week number 1 to 53 of the year this date falls in, according to the rules of
     * ISO standard IS-8601 section 5.5. A week that lies partly in one year
     * and partly in another is assigned a number in the year in which most
     * of its days lie. This means that
     * week 1 of any year is the week that contains 4 January,
     * or equivalently
     * week 1 of any year is the week that contains the first
     * Thursday in January. Most years have 52 weeks, but years
     * that start on a Thursday and leap
     * years that start on a Wednesday have 53 weeks.
     * Jan 1 may well be in week 53 of the previous year!
     * Only defined for dates on or after 1600 Jan 01.
     *
     * @return week number 1..53, 0 for null or invalid date.
     * @see <a href="http://www.pip.dknet.dk/~pip10160/calendar.faq3.txt">Calendar FAQ</A>
     */
    public final int getISOWeekNumber() {
        if (ordinal < Jan_01_Leap100RuleYear) return 0;
        int jan04Ordinal = toOrdinal(yyyy, 1, 4);
        int jan04DayOfWeek = (jan04Ordinal + MondayIsZeroAdjustment - MIN_ORDINAL) % 7; // 0=Monday 6=Sunday
        int week1StartOrdinal = jan04Ordinal - jan04DayOfWeek;
        if (ordinal < week1StartOrdinal) { // we are part of the previous year. Don't worry about year 0.
            jan04Ordinal = toOrdinal(yyyy - 1, 1, 4);
            jan04DayOfWeek = (jan04Ordinal + MondayIsZeroAdjustment - MIN_ORDINAL) % 7; // 0=Monday 6=Sunday
            week1StartOrdinal = jan04Ordinal - jan04DayOfWeek;
        } else if (mm == 12) { // see if we are part of next year. Don't worry about year 0.
            jan04Ordinal = toOrdinal(yyyy + 1, 1, 4);
            jan04DayOfWeek = (jan04Ordinal + MondayIsZeroAdjustment - MIN_ORDINAL) % 7; // 0=Monday 6=Sunday
            int week1StartNextOrdinal = jan04Ordinal - jan04DayOfWeek;
            if (ordinal >= week1StartNextOrdinal) week1StartOrdinal = week1StartNextOrdinal;
        }
        return ((ordinal - week1StartOrdinal) / 7) + 1;
    } // end toISOWeekNumber

    /**
     * get day of month for this nl.pwy.pdxreader.BigDate.
     *
     * @return day 1 to 31, 0 for null date.
     */
    public final int getDD() {
        return dd;
    }

    /**
     * Get day number in the year for this nl.pwy.pdxreader.BigDate.
     *
     * @return day number Jan 01 = 1, 1 to 366
     */
    public final int getDDD() {
        return (ordinal == NULL_ORDINAL) ? 0 : (ordinal - jan01OfYear(yyyy) + 1);
    }

    /**
     * Get month of year for this nl.pwy.pdxreader.BigDate.
     *
     * @return month 1 to 12, 0 for null date.
     */
    public final int getMM() {
        return mm;
    }

    /**
     * get days since 1970 Jan 01 for this nl.pwy.pdxreader.BigDate.  1970/01/01 = day 0.
     *
     * @return days since 1970 Jan 01.
     */
    final int getOrdinal() {
        return ordinal;
    }

    /**
     * Get milliseconds since 1970 Jan 01 00:00 GMT for this nl.pwy.pdxreader.BigDate.
     * Does not account for leap seconds primarily because we do not know
     * them in advance.
     * N.B. returns long, not int as in many Unix implementations.
     * This the long that a Sun Date constructor wants.
     *
     * @return milliseconds since 1970 Jan 01 00:00 GMT
     */
    public final long getTimeStamp() {
        // 86,400,000 = 1000 * 60 * 60 * 24 = milliseconds per day
        return ordinal == NULL_ORDINAL ? NULL_TIMESTAMP : (ordinal * 86400000L);
    }

    /**
     * Get year for this nl.pwy.pdxreader.BigDate.
     *
     * @return year -999,999 to 999,999.  0 for null date.
     *         negative is BC, positive AD.
     */
    public final int getYYYY() {
        return yyyy;
    }

    /**
     * hashCode for use in Hashtable lookup
     *
     * @return the ordinal which is perfectly unique for the date.
     */
    public final int hashCode() {
        return ordinal;
    }

    /**
     * Is the given year a leap year, considering history, mod 100 and mod 400 rules?
     * By 1582, this excess of leap years had built
     * up noticeably. At the suggestion of astronomers
     * Luigi Lilio and Chistopher Clavius, Pope Gregory XIII
     * dropped 10 days from the calendar.
     * Thursday 1582 October 4 Julian was followed
     * immediately by Friday 1582 October 15 Gregorian.
     * He decreed that every 100 years, a leap year should
     * be dropped except that every 400 years the leap year
     * should be restored. Only Italy, Poland, Portugual
     * and Spain went along with the new calendar immediately.
     * One by one other countries adopted it in different years.
     * Britain and its territories (including the USA and Canada)
     * adopted it in 1752. By then, 11 days had to be dropped.
     * 1752 September 2 was followed immediately by
     * 1752 September 14. The Gregorian calendar is the most
     * widely used scheme. This is the scheme endorsed by
     * the US Naval observatory. It corrects the year to 365.2425.
     * It gets ahead 1 day every 3289 years.
     * If you wanted to use a different leap year routine, you would
     * need to make extensive changes all through nl.pwy.pdxreader.BigDate.
     *
     * @param yyyy year to test.
     * @return true if the year is a leap year.
     */
    private static boolean isLeap(int yyyy) {
        if (yyyy < Leap100RuleYYYY) return yyyy % 4 == 0;
        if (yyyy % 4 != 0) return false;
        if (yyyy % 100 != 0) return true;
        return yyyy >= Leap400RuleYYYY && yyyy % 400 == 0;
    }

    /**
     * Test to see if the given yyyy, mm, dd date is legitimate.
     * Does extensive checks considering leap years, missing days etc.
     *
     * @param yyyy -999,999 (BC) to +999,999 (AD)
     * @param mm   month 1 to 12 (not 0 to 11 as in Sun's Date)
     * @param dd   day 1 to 31
     * @return true if yyyy mm dd is a valid date.
     */
    private static boolean isValid(int yyyy, int mm, int dd) {
        // null date 0000 00 00 is considered valid
        // but otherwise year 0000 never happened.
        if (yyyy == 0) return (mm == 0) && (dd == 0);
        if ((yyyy < MIN_YEAR) || (yyyy > MAX_YEAR)
                || (mm < 1) || (mm > 12)
                || (dd < 1) || (dd > 31)) return false;
        // account for missing 10 days in 1582.
        // Thursday 1582 October 4 Julian was followed
        // immediately by Friday 1582 October 15
        // Similarly for the British Calendar
        return !(yyyy == OJC_lastYYYY && mm == OJC_lastMM && OJC_lastDD < dd && dd < GC_firstDD) && dd <= daysInMonth(mm, isLeap(yyyy));
    } // end isValid

    /**
     * Set the ordinal field, and compute the equivalent internal
     * Gregorian yyyy mm dd fields.
     * alias setOrdinal.
     *
     * @param ordinal days since 1970 Jan 1.
     */
    public final void set(int ordinal) {
        if (this.ordinal == ordinal) return;
        this.ordinal = ordinal;
        toGregorian();
    } // end set

    /**
     * Set the ordinal field, and compute the equivalent internal
     * Gregorian yyyy mm dd fields.
     * alias set.
     *
     * @param ordinal days since 1970 Jan 1.
     */
    final void setOrdinal(int ordinal) {
        if (this.ordinal == ordinal) return;
        this.ordinal = ordinal;
        toGregorian();
    } // end set

    /**
     * Set the yyyy mm dd Gregorian fields, and compute the internal ordinal equivalent.
     * yyyy mm dd are checked for validity.
     *
     * @param yyyy -999,999 (BC) to +999,999 (AD)
     * @param mm   month 1 to 12 (not 0 to 11 as in Sun's Date)
     * @param dd   day 1 to 31
     */
    public final void set(int yyyy, int mm, int dd) {
        set(yyyy, mm, dd, CHECK);
    }

    /**
     * Set the Gregorian fields, and compute the ordinal equivalent
     * with the same modifiers CHECK, NORMALIZE, BYPASSCHECK as the constructor.
     * BEWARE! In Java a lead 0 on an integer implies OCTAL.
     *
     * @param yyyy -999,999 (BC) to +999,999 (AD)
     * @param mm   month 1 to 12 (not 0 to 11 as in Sun's Date)
     * @param dd   day 1 to 31
     * @param how  one of CHECK BYPASSCHECK NORMALIZE NORMALISE
     */
    final void set(int yyyy, int mm, int dd, int how) {
        if (this.yyyy == yyyy && this.mm == mm && this.dd == dd) return;
        this.yyyy = yyyy;
        this.mm = mm;
        this.dd = dd;
        switch (how) {
            case CHECK:
                if (!isValid(yyyy, mm, dd))
                    throw new IllegalArgumentException("invalid date: "
                            + yyyy + "/" + mm + "/" + dd);
                break;

            case NORMALISE:
                normalise();
                break;

            case BYPASSCHECK:
                break;
        } // end switch

        toOrdinal();
    } // end set

    /**
     * Returns a nl.pwy.pdxreader.BigDate object initialised to today's UTC (Greenwich) date,
     * in other words that date the people in Greenwich England think
     * it is right now.
     * It works even if Java's default Timezone is not configured correctly,
     * but it requires your system clock accurately set to UTC time.
     * Experiment setting your system date/time to various
     * values and making sure you are getting the expected results.
     * Note the date in the created object
     * does not keep updating every time you reference it with
     * methods like getOrdinal or getDD.  You always get the date the object
     * was created.
     *
     * @return BigDate object initialised to today, in Greenwich.
     */
    public static BigDate UTCToday() {
        // 86,400,000 = 1000 * 60 * 60 * 24 = milliseconds per day
        return new BigDate((int) (System.currentTimeMillis() / 86400000L));
    } // end UTCToday

    /**
     * Returns a nl.pwy.pdxreader.BigDate object initialised to today's local date.
     * It depends on Java's default Timezone being configured,
     * and your system clock accurately set to UTC time.
     * Experiment setting your system date/time to various
     * values and making sure you are getting the expected results.
     * Note the date in the created object
     * does not keep updating every time you reference it with
     * methods like getOrdinal or getDD.  You always get the date
     * the object was created. It is quite a production to get
     * the local date.  Best to ask once and save the today object.
     *
     * @return BigDate object initialised to today, local time.
     */
    public static BigDate localToday() {
        // 86,400,000 = 1000 * 60 * 60 * 24 = milliseconds per day
        long currentUTCDateTime = System.currentTimeMillis();
        BigDate d = new BigDate((int) (currentUTCDateTime / 86400000L));

        // Find out which timezone he is in, and the rules for
        // when daylight saving kicks in and out.
        TimeZone timeZone = TimeZone.getDefault();

        /**
         * Gets the time zone offset, for current date, modified in case of
         * daylight savings. This is the offset to add *to* UTC to get local time.
         * @param era the era of the given date, AD = 1
         * @param year the year in the given date.
         * @param month the month in the given date.
         * Month is 0-based. e.g., 0 for January.
         * @param day the day-in-month of the given date.
         * @param dayOfWeek the day-of-week of the given date.
         * @param milliseconds the millis in day in <em>standard</em> local time.
         * @return the offset to add *to* GMT to get local time.
         */
        int offsetInMillis = timeZone.getOffset(1,
                d.getYYYY(),
                d.getMM() - 1,
                d.getDD(),
                d.getDayOfWeek(),
                (int) (currentUTCDateTime % 86400000L));
        d.setOrdinal((int) ((currentUTCDateTime + offsetInMillis) / 86400000L));
        return d;
    } // end localToday

    /**
     * Convert date in form YYYY MM DD into days since the 1970 Jan 01.
     * This method lets you convert directly from Gregorian to
     * ordinal without creating a nl.pwy.pdxreader.BigDate object.
     * yyyy mm dd must be a valid date.
     *
     * @param yyyy -999,999 (BC) to +999,999 (AD)
     * @param mm   month 1 to 12 (not 0 to 11 as in Sun's Date)
     * @param dd   day 1 to 31
     * @return ordinal, days since 1970 Jan 01.
     */
    private static int toOrdinal(int yyyy, int mm, int dd) {
        // treat null date as a special case
        if ((yyyy == 0) && (mm == 0) && (dd == 0)) {
            return NULL_ORDINAL;
        }

        // jan01OfYear handles missing day adjustment for years > 1582
        // We only need to handle year = 1582 here.
        int missingDayAdjust =
                (yyyy == OJC_lastYYYY
                        && ((mm == OJC_lastMM
                        && dd > OJC_lastDD)
                        || mm > OJC_lastMM))
                        ? missingDays : 0;

        return jan01OfYear(yyyy)
                + daysInYearPriorToMonth(mm, isLeap(yyyy))
                - missingDayAdjust
                + dd - 1;
    } // end toOrdinal

    /**
     * Convert date to a human-readable String.
     *
     * @return this nl.pwy.pdxreader.BigDate as a String in form YYYY/MM/DD
     */
    public final String toString() {
        return (ordinal == NULL_ORDINAL) ? "" :
                Misc.toLZ(yyyy, 4) + "/" + Misc.toLZ(mm, 2) + "/" + Misc.toLZ(dd, 2);
    } // end toString

    /**
     * calculate the age in years, months and days.
     *
     * @param birthDate usually the birth of a person.
     * @param asof      usually today, the day you want the age as of.
     *                  now must come after birthDate to get a meaningful result.
     * @return array of three ints (not Integers).
     *         [0]=age in years, [1]=age in months, [2]=age in days.
     */
    public static int[] age(BigDate birthDate, BigDate asof) {
        int birthYYYY = birthDate.getYYYY();
        int birthMM = birthDate.getMM();
        int birthDD = birthDate.getDD();

        int asofYYYY = asof.getYYYY();
        int asofMM = asof.getMM();
        int asofDD = asof.getDD();

        int ageInYears = asofYYYY - birthYYYY;
        int ageInMonths = asofMM - birthMM;
        int ageInDays = asofDD - birthDD;

        if (ageInDays < 0) {
            // This does not need to be a while loop because
            // birthDD is always less than daysInbirthMM month.
            // Guaranteed after this single treatment, ageInDays will be >= 0.
            // i.e. ageInDays = asofDD - birthDD + daysInBirthMM.
            ageInDays += BigDate.daysInMonth(birthMM, BigDate.isLeap(birthYYYY));
            ageInMonths--;
        }

        if (ageInMonths < 0) {
            ageInMonths += 12;
            ageInYears--;
        }
        if (birthYYYY < 0 && asofYYYY > 0) ageInYears--;

        if (ageInYears < 0) {
            ageInYears = 0;
            ageInMonths = 0;
            ageInDays = 0;
        }
        int[] result = new int[3];
        result[0] = ageInYears;
        result[1] = ageInMonths;
        result[2] = ageInDays;
        return result;
    } // end age

    /**
     * How many days are there in a given month?
     *
     * @param mm   month 1 to 12
     * @param leap true if you are interested in a leap year
     * @return how many days are in that month
     */
    private static int daysInMonth(int mm, boolean leap) {
        if (mm != 2) return usual_DaysPerMonthTable[mm - 1];
        else return leap ? 29 : 28;
    } // end daysInMonth

    // P R O T E C T E D    M E T H O D S

    /**
     * How many days were there in the year prior to the first day of the
     * given month?
     *
     * @param mm   month 1 to 12.
     * @param leap true if you are interested in a leap year.
     * @return how many days in year prior to the start of that month.
     */
    private static int daysInYearPriorToMonth(int mm, boolean leap) {
        return leap ? leap_daysInYearPriorToMonthTable[mm - 1]
                : usual_daysInYearPriorToMonthTable[mm - 1];
    } // end daysPriorToMonth

    /**
     * Ordinal date of Jan 01 of the given year.
     *
     * @param yyyy year of interest
     * @return ordinal of Jan 01 of that year.
     */
    private static int jan01OfYear(int yyyy) {
        if (yyyy < 0)
            return (yyyy * 365 + yyyy / 4) + BC_epochAdjustment;

        int leapsInPriorYears = (yyyy + 3) / 4;

        int missingDayAdjust = (yyyy > GC_firstYYYY) ? missingDays : 0;

        // mod 100 and mod 400 rules started in 1600 for Gregorian,
        // but 1800/2000 in the British scheme
        if (yyyy > Leap100RuleYYYY) {
            leapsInPriorYears -= (yyyy - Leap100RuleYYYY + 99) / 100;
        }
        if (yyyy > Leap400RuleYYYY) {
            leapsInPriorYears += (yyyy - Leap400RuleYYYY + 399) / 400;
        }

        return yyyy * 365
                + leapsInPriorYears
                - missingDayAdjust
                + AD_epochAdjustment;
    } // end jan01OfYear

    /**
     * Convert day number ddd in year to month.
     *
     * @param ddd  day number in year Jan 01 = 1, 1 to 366.
     * @param leap true if year of interest is boolean.
     * @return month that day number would fall in.
     */
    private static int dddToMM(int ddd, boolean leap) {
        return leap ? leap_dddToMMTable[ddd - 1]
                : usual_dddToMMTable[ddd - 1];
    }

    /**
     * Clean up an invalid date, leaving the results internally.<BR>
     * e.g. 1954 September 31 -> 1954 October 1. <BR>
     * 1954 October -1 -> 1954 September 29.<BR>
     * 1954 13 01 -> 1955 01 01.
     * This lets you do year, month or day arithmetic.
     * normalise does not recompute the ordinal.
     */
    final void normalise() {
        // yyyy, mm, dd must be set at this point
        if (isValid(yyyy, mm, dd)) return;
        else if (mm > 12) {
            yyyy += (mm - 1) / 12;
            mm = ((mm - 1) % 12) + 1;
            if (isValid(yyyy, mm, dd)) return;
        } else if (mm <= 0) {
            // Java's definition of modulus means we need to handle negatives as a special case.
            yyyy -= -mm / 12 + 1;
            mm = 12 - (-mm % 12);
            if (isValid(yyyy, mm, dd)) return;
        }
        if (isValid(yyyy, mm, 1)) {
            int olddd = dd;
            dd = 1;
            toOrdinal();
            ordinal += olddd - 1;
            toGregorian();
            if (isValid(yyyy, mm, dd)) return;
        }

        throw new IllegalArgumentException("date cannot be normalised: "
                + yyyy + "/" + mm + "/" + dd);

    } // end normalise

    /**
     * converts ordinal to YYYY MM DD, leaving results internally.
     */
    final void toGregorian() {
        // ordinal must be set at this point.

        // handle the null date as a special case
        if (ordinal == NULL_ORDINAL) {
            yyyy = 0;
            mm = 0;
            dd = 0;
            return;
        }
        if (ordinal > MAX_ORDINAL)
            throw new IllegalArgumentException("invalid ordinal date: " + ordinal);
        else if (ordinal >= GC_firstOrdinal) {
            yyyy = Leap400RuleYYYY
                    + flooredMulDiv(ordinal - Jan_01_Leap400RuleYear,
                    10000,
                    3652425); /* 365 + 0.25 - 0.01 - 0.0025 */
            /* division may be done on a negative number.
     That's ok. We don't need to mess with the
     100RuleYear.  The 400RuleYear handles it all. */
        } else if (ordinal >= Jan_01_0001) {
            // Jan_01_0001 to Oct_04_1582
            yyyy = 4 + flooredMulDiv(ordinal - Jan_01_0004, 100, 36525);
            /* 365 + 0.25 */
        } else if (ordinal >= MIN_ORDINAL) {
            // LowestDate to Dec_31_0001BC
            yyyy = -4 + flooredMulDiv(ordinal - Jan_01_0004BC, 100, 36525);
            /* 365 + 0.25 */
        } else throw new IllegalArgumentException("invalid ordinal date: " + ordinal);

        int ddd = ordinal - jan01OfYear(yyyy) + 1;
        if (ddd <= 0) {
            // our approximation was too high
            yyyy--;
            ddd = ordinal - jan01OfYear(yyyy) + 1;
        }
        boolean leap = isLeap(yyyy);
        if (ddd > (leap ? 366 : 365)) {
            // our approximation was too low
            yyyy++;
            ddd = ordinal - jan01OfYear(yyyy) + 1;
            leap = isLeap(yyyy);
        }

        // Symantec Visual Cafe Pro bug, cannot write as:
        // Oct_15_1582 <= ordinal && ordinal <= Dec_31_1582
        if (ordinal >= GC_firstOrdinal
                && ordinal <= GC_firstDec_31) ddd += missingDays;

        mm = dddToMM(ddd, leap);
        dd = ddd - daysInYearPriorToMonth(mm, leap);

        // at this point yyyy, mm and dd have been computed.
    } // end toGregorian

    /**
     * Convert date in form YYYY MM DD into days since the epoch,
     * leaving results internally.
     */
    final void toOrdinal() {
        ordinal = toOrdinal(yyyy, mm, dd);
    } // end toOrdinal

    // p r i v a t e   m e t h o d s

    /**
     * read back a serialized nl.pwy.pdxreader.BigDate object and reconstruct the missing
     * transient fields.
     * readObject leaves results in this.  It does not create a new object.
     *
     * @param s stream to read from.
     * @throws ClassNotFoundException
     * @throws java.io.IOException
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.lang.ClassNotFoundException,
            java.io.IOException {
        s.defaultReadObject();
        try {
            toGregorian(); // restore transient fields
        } catch (IllegalArgumentException e) {
            throw new java.io.IOException("bad serialized nl.pwy.pdxreader.BigDate");
        }
    } // end readObject

    /**
     * Serialize and write the nl.pwy.pdxreader.BigDate object.
     * Only the ordinal will be written. Other fields are transient.
     *
     * @param s stream to write to
     * @throws java.lang.ClassNotFoundException
     *                             standard
     * @throws java.io.IOException standard
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.lang.ClassNotFoundException,
            java.io.IOException {
        s.defaultWriteObject();
    }
    // p u b l i c   v a r i a b l e s   a n d   c o n s t a n t s

    /**
     * Constant: when passed to a constructor, it means caller guarantees YYYY MM DD
     * are valid including leap year effects and missing day effects.
     * nl.pwy.pdxreader.BigDate will not bother to check them.
     */
    private static final int BYPASSCHECK = 1;

    /**
     * constant: when passed to a contructor it means nl.pwy.pdxreader.BigDate should check that YYYY MM DD are valid.
     */
    private static final int CHECK = 0;

    /**
     * Constant: true if you want the British calender, false if
     * Pope Gregory's. You must recompile for it to have effect.
     * default false.
     */
    private static final boolean isBritish = false;

    /**
     * Constant: biggest year that nl.pwy.pdxreader.BigDate handles, 999,999 AD.
     */
    private static final int MAX_YEAR = 999999;

    /**
     * Constant: earliest year that nl.pwy.pdxreader.BigDate handles, 999,999 BC.
     */
    private static final int MIN_YEAR = -999999;

    /**
     * Constant: ordinal to represent a null date -2,147,483,648, null Gregorian is 0,0,0.
     */
    private static final int NULL_ORDINAL = Integer.MIN_VALUE;

    /**
     * Constant : value for a null TimeStamp -9,223,372,036,854,775,808
     */
    private static final long NULL_TIMESTAMP = Long.MIN_VALUE;

    /**
     * Constant: when passed to a constructor, it means any invalid dates are
     * converted into the equivalent valid one.<BR>
     * e.g. 1954 September 31 -> 1954 October 1. <BR>
     * 1954 October -1 -> 1954 September 29.<BR>
     * 1954 13 01 -> 1955 01 01.
     */
    private static final int NORMALISE = 2;

    /**
     * Constant: American spelling alias for NORMALISE.
     *
     * @see #NORMALISE
     */
    static public final int NORMALIZE = NORMALISE;

    // P R O T E C T E D   I N S T A N C E   V A R I A B L E S

    /**
     * Day, 1 to 31.
     * If size of nl.pwy.pdxreader.BigDate objects were a consideration, you could make this a byte.
     */
    private transient int dd = 0;

    /**
     * Month, 1 to 12.
     * If size of nl.pwy.pdxreader.BigDate objects were a consideration, you could make this a byte.
     */
    private transient int mm = 0;

    /**
     * Ordinal days since Jan 01, 1970.
     * -365968798 to 364522971.
     * i.e. 999,999 BC to 999,999 AD.
     */
    private int ordinal = NULL_ORDINAL;

    /**
     * Year, -999,999 to +999,999, negative is BC, positive is AD, 0 is null.
     */
    private transient int yyyy = 0;

    // P R I V A T E   C O N S T A N T S

    /**
     * embedded copyright notice
     */
    private static final String copyright =
            "nl.pwy.pdxreader.BigDate 2.2 shareware copyright 1997-1998 by Roedy Green Canadian Mind Products, roedy@mindprod.com";

    /**
     * Constant: year of the last date (1582 Oct 4) of the old Julian
     * calendar, just prior to the missing
     * 10 days, = 1852.  Different parts of the world made the transition
     * at different times. Usually 1582 Oct 4. For British calender
     * it would be 1752 Sep 2.
     */
    private static final int OJC_lastYYYY = isBritish ? 1752 : 1582;

    /**
     * Constant: month  of the last date (1582 Oct 4) of the old Julian
     * calendar, = 10.
     */
    private static final int OJC_lastMM = isBritish ? 9 : 10;

    /**
     * Constant: day of the last date (1582 Oct 4) of the old Julian
     * calendar, = 4.
     */
    private static final int OJC_lastDD = isBritish ? 2 : 4;

    /**
     * Constant: year of the first date (1752 Oct 15) of the Gregorian Calendar = 1582.
     * Just after the missing
     * 10 days, = 1582.  Different parts of the world made the transition
     * at different times. Usually 1752 Oct 15. For British calender
     * it would be 1752 Sep 14.
     */
    private static final int GC_firstYYYY = isBritish ? 1752 : 1582;

    /**
     * Constant: month of the first date (1752 Oct 15) of the Gregorian Calendar = 10.
     */
    private static final int GC_firstMM = isBritish ? 9 : 10;

    /**
     * Constant: day of the first date (1752 Oct 15) of the Gregorian Calendar = 15.
     */
    private static final int GC_firstDD = isBritish ? 14 : 15;

    /**
     * Constant: year that the mod 100 rule first had any effect.
     * For The Gregorian Calendar, 1600.  For the British Calendar,
     * 1800. Round up to next 100 years after the missing day
     * anomaly.
     */
    private static final int Leap100RuleYYYY =
            ((GC_firstYYYY + 99) / 100) * 100;

    /**
     * Constant: year that the mod 400 rule first had any effect.
     * For The Gregorian Calendar, 1600.  For the British Calendar,
     * 2000. Round up to next 400 years after the missing day
     * anomaly.
     */
    private static final int Leap400RuleYYYY =
            ((GC_firstYYYY + 399) / 400) * 400;

    /**
     * Constant: how many days were lost during the Gregorian correction,
     * 10 for the Gregorian calendar, 11 for the British.
     */
    private static final int missingDays = GC_firstDD - OJC_lastDD - 1;

    /**
     * Constant: adjustment to make ordinal 0 come out on 1970 Jan 01
     * for AD date calculations.
     * This number was computed by making an estimate, seeing what value
     * toOrdinal gave for 1970/01/01 and then adjusting this constant
     * so that 1970/01/01 would come out Ordinal 0.
     */
    private static final int AD_epochAdjustment = -719530;

    /**
     * Constant: adjustment to make ordinal 0 come out to 1970 Jan 01
     * for BC date calculations.
     */
    private static final int BC_epochAdjustment = AD_epochAdjustment + 366;

    /**
     * Adjustment to make Sunday come out as day 0 after doing 7 modulus.
     * Accounts for fact MIN_ORDINAL was not a Sunday.
     */
    private static final int SundayIsZeroAdjustment = 5;

    /**
     * Adjustment to make Monday come out as day 0 after doing 7 modulus.
     * Accounts for fact MIN_ORDINAL was not a Monday
     */
    private static final int MondayIsZeroAdjustment = 4;

    // P R I V A T E   T A B L E S   F O R   D A T E   C O N V E R S I O N

    /**
     * Constant array: how many days are in there in a month, (not a leap year).
     * Indexed by Jan = 0.
     */
    private static final int[] usual_DaysPerMonthTable =
            {31, 28, 31,   /* J F M */
                    30, 31, 30,   /* A M J */
                    31, 31, 30,   /* J A S */
                    31, 30, 31}; /* O N D */

    /**
     * Constant array: how many days in the year prior to the first of the
     * given month in a non-leap year.  Indexed by Jan=0.
     */
    private static final int[] usual_daysInYearPriorToMonthTable =
            {0, 31, 59,       /* J F M */
                    90, 120, 151,    /* A M J */
                    181, 212, 243,   /* J A S */
                    273, 304, 334}; /* O N D */

    /**
     * Constant array: how many days in the year prior to the first of the
     * given month in a leap year.  Indexed by Jan=0.
     */
    private static final int[] leap_daysInYearPriorToMonthTable =
            {0, 31, 60,      /* J F M */
                    91, 121, 152,   /* A M J */
                    182, 213, 244,  /* J A S */
                    274, 305, 335}; /* O N D */

    /**
     * Constant array: what month does the indexing day number fall in
     * in a non-leap year? Indexed by ddd Jan 1 = 0.
     */
    private static final int[] usual_dddToMMTable;

    static {
        // <clinit> static initialisation code for usual_dddToMMTable
        usual_dddToMMTable = new int[365];
        int ddd = 0;
        for (int mm = 1; mm <= 12; mm++) {
            int last = daysInMonth(mm, false);
            for (int dd = 0; dd < last; dd++) {
                usual_dddToMMTable[ddd++] = mm;
            } // end for dd
        } // end for mmm
    } // end static init

    /**
     * Constant array: what month does the indexing day number fall in
     * in a leap year? Indexed by ddd Jan 1 = 0.
     */
    private static final int[] leap_dddToMMTable;

    static {
        // <clinit> static initialisation code for leap_dddToMMTable
        leap_dddToMMTable = new int[366];
        int ddd = 0;
        for (int mm = 1; mm <= 12; mm++) {
            int last = daysInMonth(mm, true);
            for (int dd = 0; dd < last; dd++) {
                leap_dddToMMTable[ddd++] = mm;
            }
        }
    } // end static init

    // Various constant Ordinal dates needed for toGregorian.
    // Not to worry, these definitions are not circular.
    // toOrdinal does not use these values.
    // They are roughly in ascending order.

    /**
     * Constant: earliest ordinal that nl.pwy.pdxreader.BigDate handles; corresponds to 999,999 Jan 01 BC.
     */
    private static final int MIN_ORDINAL
            = BigDate.toOrdinal(MIN_YEAR, 1, 1);
    // don't move this up with other publics since it requires privates
    // for its definition.

    /**
     * Constant: Ordinal for 4 BC Jan 01
     */
    private static final int Jan_01_0004BC = BigDate.toOrdinal(-4, 1, 1);

    /**
     * Constant: Ordinal for 1 BC Jan 01
     */
    private static final int Jan_01_0001BC = BigDate.toOrdinal(-1, 1, 1);

    /**
     * Constant: Ordinal for 1 AD Jan 01
     */
    private static final int Jan_01_0001 = BigDate.toOrdinal(1, 1, 1);

    /**
     * Constant: Ordinal for 4 AD Jan 01
     */
    private static final int Jan_01_0004 = BigDate.toOrdinal(4, 1, 1);

    /**
     * Constant: Ordinal for 1582 Oct 15, the first day of the Gregorian calendar.
     */
    private static final int GC_firstOrdinal
            = BigDate.toOrdinal(
            GC_firstYYYY,
            GC_firstMM,
            GC_firstDD);
    /**
     * Constant: Ordinal for 1582 Dec 31, the last day of first year of the Gregorian calendar.
     */
    private static final int GC_firstDec_31
            = BigDate.toOrdinal(GC_firstYYYY,
            12, 31);

    /**
     * Constant: ordinal of 1600 Jan 01, the first year when the mod 100 leap year rule first had any effect.
     */
    private static final int Jan_01_Leap100RuleYear
            = BigDate.toOrdinal(Leap100RuleYYYY, 1, 1);

    /**
     * Constant: ordinal of 1600 Jan 01, the year when the mod 400 leap year rule first had any effect.
     */
    private static final int Jan_01_Leap400RuleYear
            = BigDate.toOrdinal(Leap400RuleYYYY, 1, 1);

    /**
     * Constant: biggest ordinal that nl.pwy.pdxreader.BigDate will accept, corresponds to 999,999 Dec 31 AD.
     */
    private static final int MAX_ORDINAL
            = BigDate.toOrdinal(MAX_YEAR, 12, 31);
    // don't move this up with other publics since it requires privates
    // for its definition.

}
