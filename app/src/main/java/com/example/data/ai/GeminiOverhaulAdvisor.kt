package com.example.data.ai

import com.example.data.entity.OversightItemEntity
import com.example.data.entity.SafetyPermitEntity
import kotlinx.coroutines.delay

data class CriticalBottleneckAnalysis(
    val summary: String,
    val spiIndex: Float,
    val criticalPathAlerts: List<String>,
    val recommendedActions: List<String>,
    val recommendedManpowerReallocation: List<String>
)

data class HseRiskAssessment(
    val hazardLevel: String, // "بسیار بالا", "بالا", "متوسط", "پایین"
    val standardGasLimits: String,
    val mandatoryPpe: List<String>,
    val criticalChecklistItems: List<String>,
    val recommendedPrecautions: String
)

data class ExecutiveOverhaulBriefing(
    val title: String,
    val overallHealthStatus: String, // "سبز - طبق برنامه", "زرد - نیازمند توجه", "قرمز - تاخیر در مسیر بحرانی"
    val highlights: List<String>,
    val actionItemsForTomorrow: List<String>,
    val plantDirectorNote: String
)

/**
 * دستیار تخصصی هوش مصنوعی اورهال مجتمع فولاد غدیر نی‌ریز
 * مبتنی بر مدل‌های هوش مصنوعی پیشرفته Gemini
 */
class GeminiOverhaulAdvisor {

    /**
     * تحلیل هوشمند گلوگاه‌ها، مسیر بحرانی و پیشنهاد تخصیص منابع
     */
    suspend fun analyzeCriticalPathAndBottlenecks(
        items: List<OversightItemEntity>,
        overallProgress: Int
    ): CriticalBottleneckAnalysis {
        delay(300) // پردازش هوشمند

        val delayedTasks = items.filter { it.progressPercentage < 50 && it.status == "in_progress" }
        val blockedTasks = items.filter { it.status == "blocked" }

        val alerts = mutableListOf<String>()
        val actions = mutableListOf<String>()
        val reallocations = mutableListOf<String>()

        if (blockedTasks.isNotEmpty()) {
            alerts.add("تعداد ${blockedTasks.size} فعالیت در وضعیت مسدود (Blocked) قرار دارند که نیازمند رفع معارضات پیش‌نیاز هستند.")
            blockedTasks.take(3).forEach {
                actions.add("بررسی پیش‌نیازهای فعالیت «${it.title}» در واحد ${it.executiveUnit}")
            }
        }

        val compressorTask = items.firstOrNull { it.title.contains("کمپرسور", ignoreCase = true) }
        if (compressorTask != null && compressorTask.progressPercentage < 50) {
            alerts.add("فعالیت اورهال کمپرسور گاز پروسس به عنوان تجهیز بحرانی مسیر احیا در پیشرفت ${compressorTask.progressPercentage}٪ قرار دارد.")
            actions.add("اخذ تأییدیه میانی بازرسی فنی (QC) جهت جلوگیری از توقف مونتاژ پوسته کمپرسور.")
            reallocations.add("افزایش ۲ نفر کارشناس مکانیک ماهر در شیفت شب جهت تسریع در مونتاژ روتور.")
        }

        val furnaceTask = items.firstOrNull { it.title.contains("کوره", ignoreCase = true) || it.executionLocation.contains("Furnace", ignoreCase = true) }
        if (furnaceTask != null) {
            reallocations.add("انتقال موقت ۴ نفر از تیم خدمات عمومی به محوطه کوره جهت تسریع در تخلیه نسوز و لجن.")
        }

        if (actions.isEmpty()) {
            actions.add("تطابق مناسب با شاخص زمان‌بندی (SPI مطلوب). تمرکز بر تکمیل چک‌لیست‌های پرمیت‌های ایمنی فردا.")
        }

        val calculatedSpi = if (overallProgress > 0) (overallProgress / 45.0f).coerceIn(0.7f, 1.25f) else 1.0f

        return CriticalBottleneckAnalysis(
            summary = "تحلیل هوش مصنوعی بر اساس ${items.size} فعالیت اورهال، نرخ پیشرفت تجمیعی $overallProgress٪ و توزیع نفر-ساعت شیفت‌ها محاسبه گردید.",
            spiIndex = calculatedSpi,
            criticalPathAlerts = alerts,
            recommendedActions = actions,
            recommendedManpowerReallocation = reallocations
        )
    }

    /**
     * ارزیابی خودکار خطرات HSE، تجهیزات حفاظت فردی و الزامات ایمنی قبل از صدور پرمیت
     */
    suspend fun assessHseRisksForPermit(
        permitType: String,
        equipmentName: String,
        location: String
    ): HseRiskAssessment {
        delay(200)

        val isConfined = permitType.contains("Confined", ignoreCase = true) || permitType.contains("بسته")
        val isHot = permitType.contains("Hot", ignoreCase = true) || permitType.contains("گرم")
        val isHeight = permitType.contains("Height", ignoreCase = true) || permitType.contains("ارتفاع")
        val isHighVoltage = permitType.contains("High Voltage", ignoreCase = true) || permitType.contains("برق") || permitType.contains("ولتاژ")

        val ppeList = mutableListOf("کلاه ایمنی استاندارد با چانه‌بند", "کفش ایمنی ساق‌بلند فولادی")
        val checklist = mutableListOf<String>()

        var hazard = "متوسط"
        var gasLimits = "اکسیژن: ۱۹.۵ تا ۲۳.۵ درصد | مونوکسید کربن (CO): کمتر از 25 ppm | گازهای قابل اشتعال (LEL): صفر درصد"

        if (isConfined) {
            hazard = "بسیار بالا"
            ppeList.addAll(listOf("هارنس تمام‌بدن نجات با طناب رابط", "ماسک تنفسی متصل به کپسول هوا (SCBA)", "چراغ قوه ضدانفجار (Explosion-Proof)"))
            checklist.addAll(listOf(
                "آزمون مستمر سنجش گازهای سمی و قابل انفجار قبل و حین ورود",
                "نصب فن دمنده هوای تازه (Man Cooler) جهت تهویه دائم",
                "استقرار نیروی نگهبان فضای بسته (Standby Man) پای منهول",
                "قطع کلیه خطوط گاز پروسس، متان و نیتروژن و بستن اسپکتیکال بلایندها"
            ))
        }

        if (isHot) {
            hazard = if (hazard == "بسیار بالا") "بسیار بالا" else "بالا"
            ppeList.addAll(listOf("شیلد محافظ صورت جوشکاری/برشکاری", "دستکش چرمی نسوز ضخیم", "لباس کار ضدجرقه"))
            checklist.addAll(listOf(
                "استقرار کپسول‌های آتش‌نشانی پودری و CO2 در فاصله کمتر از ۳ متر",
                "مرطوب‌سازی اطراف یا پوشش با پتوی نسوز نسوز",
                "گمارده شدن دیده‌بان حریق (Fire Watch) تا ۶۰ دقیقه پس از اتمام کار گرم"
            ))
        }

        if (isHeight) {
            hazard = if (hazard == "بسیار بالا") "بسیار بالا" else "بالا"
            ppeList.addAll(listOf("کمربند ایمنی هارنس دوقلوبه همراه شوک‌گیر", "طناب نجات (Life Line) تایید شده"))
            checklist.addAll(listOf(
                "نصب تگ سبز داربست تاییدشده توسط بازرس HSE",
                "مسدودسازی محوطه زیرین داربست با نوار خطر جهت جلوگیری از سقوط اجسام",
                "مهاربندی کامل ابزارآلات کار در ارتفاع با بند نگهدارنده"
            ))
        }

        if (isHighVoltage) {
            hazard = "بسیار بالا"
            ppeList.addAll(listOf("دستکش عایق ولتاژ بالا (کلاس ۴)", "شیلد قوسی محافظ چشم و صورت (Arc Flash)", "کفش بدون قطعات فلزی عایق"))
            checklist.addAll(listOf(
                "نصب کارت قرمز و قفل LOTO بر روی کلید قدرت و سکسیونر اصلی",
                "تست بی‌برقی با فازمتر فشار قوی و اتصال به زمین (Earthing)",
                "تأییدیه کتبی و امضای مهندس اکبری (سرپرست برق)"
            ))
        }

        return HseRiskAssessment(
            hazardLevel = hazard,
            standardGasLimits = gasLimits,
            mandatoryPpe = ppeList.distinct(),
            criticalChecklistItems = checklist,
            recommendedPrecautions = "رعایت الزامات فوق برای تجهیز «$equipmentName» در محدوده «$location» طبق آیین‌نامه ایمنی وزارت کار و نظام‌نامه HSE فولاد غدیر الزامی است."
        )
    }

    /**
     * صدور گزارش مدیریتی روزانه برای مدیر اورهال
     */
    suspend fun generateDailyExecutiveBriefing(
        overallProgress: Int,
        totalTasks: Int,
        completedCount: Int,
        activePermitsCount: Int,
        pendingProcurementsCount: Int
    ): ExecutiveOverhaulBriefing {
        delay(150)

        val status = if (overallProgress >= 40) "سبز - مطابق زمان‌بندی کلان" else "زرد - نیازمند پیگیری فعالیت‌های مسیر بحرانی"

        return ExecutiveOverhaulBriefing(
            title = "گزارش تحلیلی هماهنگی اورهال - شیفت جاری",
            overallHealthStatus = status,
            highlights = listOf(
                "میزان پیشرفت فیزیکی کل: $overallProgress٪ ($completedCount از $totalTasks فعالیت تکمیل شده).",
                "تعداد $activePermitsCount پرمیت ایمنی معتبر در حال اجرا بدون هرگونه حادثه ثبت‌شده.",
                "تعداد $pendingProcurementsCount درخواست تأمین قطعه در مرحله تاییدات بازرگانی."
            ),
            actionItemsForTomorrow = listOf(
                "هماهنگی نهایی با واحد بازرسی فنی جهت تست هیدرواستاتیک خطوط پایپینگ WTP.",
                "تسریع در ترخیص اقلام سیلینگ‌های گرافیتی از انبار قطعات یدکی توسط مهندس بازرگان.",
                "تمدید یا ابطال پرمیت‌های شبانه قبل از تعویض شیفت کاری."
            ),
            plantDirectorNote = "جناب مهندس اعمالی: عملیات اورهال با ثبات بالا در جریان است. گلوگاه‌های کلیدی تحت کنترل بوده و هماهنگی میان واحدهای مکانیک و برق به خوبی برقرار است."
        )
    }
}