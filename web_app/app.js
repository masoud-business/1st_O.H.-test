// Mock Dataset for Ghadir Neyriz Steel Overhaul (فولاد غدیر نی‌ریز)
const USERS = [
  { id: 1, name: "مهندس اعمالی", role: "مدیر ارشد اورهال", unit: "مدیریت اورهال", color: "bg-blue-600" },
  { id: 2, name: "مهندس محب ایران", role: "سرپرست ارشد HSE", unit: "ایمنی و بهداشت", color: "bg-emerald-600" },
  { id: 3, name: "مهندس خاکی", role: "واحد بازرسی فنی (QC)", unit: "کنترل کیفیت", color: "bg-purple-600" },
  { id: 4, name: "مهندس بازرگان", role: "نماینده بازرگانی", unit: "تأمین کالا", color: "bg-amber-600" },
  { id: 5, name: "مهندس اکبری", role: "سرپرست واحد برق و LOTO", unit: "برق", color: "bg-cyan-600" },
  { id: 6, name: "سرپرست شیفت مکانیک", role: "ناظر اجرایی کارگاه", unit: "مکانیک", color: "bg-orange-600" }
];

let currentUser = USERS[0];

let TASKS = [
  { id: 1, wbs: "1.1", title: "دمونتاژ قسمت میانی شارژ هاپر و اسلایدگیت بالا", unit: "مکانیک", area: "Core Area", progress: 60, status: "in_progress", hours: 25, needQc: true, qcApproved: false },
  { id: 2, wbs: "1.2", title: "تخلیه کامل داخل کوره احیا و لجن‌زدایی", unit: "مکانیک", area: "Furnace", progress: 100, status: "completed", hours: 12, needQc: false, qcApproved: true },
  { id: 3, wbs: "1.3", title: "باز کردن منهول‌های کوره احیا و تهویه گاز", unit: "مکانیک", area: "Furnace", progress: 80, status: "in_progress", hours: 5, needQc: false, qcApproved: false },
  { id: 4, wbs: "2.1", title: "اورهال کمپرسور گاز پروسس و تعویض روتور و سیلینگ", unit: "مکانیک", area: "Compressor", progress: 20, status: "in_progress", hours: 50, needQc: true, qcApproved: false },
  { id: 5, wbs: "3.1", title: "سرویس و تست سوئیچ‌گیرهای ۳۳ کیلوولت (LOTO)", unit: "برق", area: "Substation", progress: 50, status: "in_progress", hours: 8, needQc: false, qcApproved: false },
  { id: 6, wbs: "4.1", title: "تخلیه آب و لجن‌زدایی کف کلاریفایر تصفیه آب (WTP)", unit: "خدمات", area: "WTP", progress: 15, status: "in_progress", hours: 40, needQc: false, qcApproved: false }
];

let PERMITS = [
  { id: 1, no: "HSE-1404-204", type: "کار در فضای بسته", location: "داخل کوره احیا", status: "issued", o2: "20.9%", co: "0 ppm", lel: "0%", loto: true, ppe: "هارنس نجات، ماسک کپسول‌دار، دمنده هوا" },
  { id: 2, no: "HSE-1404-309", type: "کار گرم و جوشکاری", location: "سقف شارژ هاپر", status: "issued", o2: "20.8%", co: "2 ppm", lel: "0%", loto: false, ppe: "شیلد جوشکاری، کپسول آتش‌نشانی، دیده‌بان حریق" },
  { id: 3, no: "HSE-1404-412", type: "ایزولاسیون برقی و LOTO", location: "پست ۳۳ کیلوولت - فیدر ۲", status: "issued", o2: "-", co: "-", lel: "-", loto: true, ppe: "دستکش عایق ۱۰۰۰ ولت، کارت قرمز برق" }
];

// Initialize UI
document.addEventListener('DOMContentLoaded', () => {
  lucide.createIcons();
  renderUserMenu();
  renderKpis();
  renderUnitBreakdown();
  renderWbsTasks();
  renderPermits();
  setupEvents();
});

function setupEvents() {
  // Navigation
  document.querySelectorAll('.nav-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const targetTab = btn.getAttribute('data-tab');
      document.querySelectorAll('.tab-content').forEach(el => el.classList.add('hidden'));
      document.getElementById(targetTab).classList.remove('hidden');

      document.querySelectorAll('.nav-btn').forEach(b => {
        b.classList.remove('text-blue-400', 'font-bold');
        b.classList.add('text-slate-400');
      });
      btn.classList.remove('text-slate-400');
      btn.classList.add('text-blue-400', 'font-bold');
      lucide.createIcons();
    });
  });

  // User Dropdown
  document.getElementById('btnUserMenu').addEventListener('click', () => {
    document.getElementById('userDropdown').classList.toggle('hidden');
  });

  // Modals
  document.getElementById('btnAiAdvisor').addEventListener('click', () => {
    document.getElementById('modalAiAdvisor').classList.remove('hidden');
  });

  document.getElementById('btnNewPermit').addEventListener('click', () => {
    document.getElementById('modalNewPermit').classList.remove('hidden');
  });

  // AI Assess in Permit Modal
  document.getElementById('btnAiAssessPermit').addEventListener('click', () => {
    const type = document.getElementById('permitTypeSelect').value;
    const btn = document.getElementById('btnAiAssessPermit');
    btn.innerText = 'در حال ارزیابی...';
    setTimeout(() => {
      if (type.includes('بسته')) {
        document.getElementById('permitPpeInput').value = 'هارنس نجات، ماسک متصل به کپسول هوا، فن دمنده هوای تازه (Man Cooler) و استقرار نگهبان منهول';
        document.getElementById('gasO2').value = '20.9%';
        document.getElementById('gasCO').value = '0 ppm';
        document.getElementById('gasLEL').value = '0%';
      } else if (type.includes('گرم')) {
        document.getElementById('permitPpeInput').value = 'شیلد محافظ صورت، دستکش چرمی نسوز، استقرار کپسول پودری و استقرار دیده‌بان آتش (Fire Watch)';
      } else if (type.includes('LOTO') || type.includes('برق')) {
        document.getElementById('permitPpeInput').value = 'دستکش عایق ولتاژ بالا، شیلد ضدقوس (Arc Flash)، نصب قفل قرمز LOTO روی سکسیونر اصلی';
      }
      btn.innerText = 'تکمیل شد ✓';
    }, 400);
  });

  // Submit New Permit
  document.getElementById('btnSubmitPermit').addEventListener('click', () => {
    const type = document.getElementById('permitTypeSelect').value;
    const location = document.getElementById('permitLocationInput').value;
    const ppe = document.getElementById('permitPpeInput').value;
    const o2 = document.getElementById('gasO2').value;
    const co = document.getElementById('gasCO').value;
    const lel = document.getElementById('gasLEL').value;

    const newP = {
      id: PERMITS.length + 1,
      no: `HSE-1404-${Math.floor(Math.random() * 800 + 100)}`,
      type: type.split('(')[0].trim(),
      location: location,
      status: "issued",
      o2: o2,
      co: co,
      lel: lel,
      loto: type.includes('LOTO'),
      ppe: ppe
    };
    PERMITS.unshift(newP);
    closeModal('modalNewPermit');
    renderPermits();
    renderKpis();
    alert(`پرمیت شماره ${newP.no} با تایید مهندس محب ایران صادر گردید.`);
  });

  // MSP Export
  document.getElementById('btnExportMsp').addEventListener('click', () => {
    const csvHeader = "ID,Task Name,Duration,Outline Level,% Work Complete,Executive Unit,Notes\n";
    const csvRows = TASKS.map(t => `${t.id},"${t.title}",${t.hours} hrs,2,${t.progress}%,"${t.unit}","ثبت کارکرد روزانه"`).join("\n");
    const preview = document.getElementById('mspExportPreview');
    preview.innerText = csvHeader + csvRows;
    preview.classList.remove('hidden');
    alert("خروجی CSV هماهنگی پایان روز با MS Project با موفقیت تولید گردید.");
  });

  // WBS Filter
  document.getElementById('wbsUnitFilter').addEventListener('change', (e) => {
    renderWbsTasks(e.target.value, document.getElementById('wbsSearchInput').value);
  });

  document.getElementById('wbsSearchInput').addEventListener('input', (e) => {
    renderWbsTasks(document.getElementById('wbsUnitFilter').value, e.target.value);
  });
}

function renderUserMenu() {
  const container = document.getElementById('usersListContainer');
  container.innerHTML = USERS.map(u => `
    <button onclick="switchUser(${u.id})" class="w-full text-right px-2.5 py-1.5 rounded-lg hover:bg-slate-700/60 flex items-center justify-between transition ${currentUser.id === u.id ? 'bg-slate-700 text-white font-bold' : 'text-slate-300'}">
      <div>
        <div class="font-bold">${u.name}</div>
        <div class="text-[10px] text-slate-400">${u.role} (${u.unit})</div>
      </div>
      <span class="w-2 h-2 rounded-full ${u.color}"></span>
    </button>
  `).join('');
}

function switchUser(id) {
  currentUser = USERS.find(u => u.id === id);
  document.getElementById('currentUserName').innerText = `${currentUser.name} (${currentUser.role.split(' ')[0]})`;
  document.getElementById('userDropdown').classList.add('hidden');
  renderUserMenu();
}

function renderKpis() {
  const completed = TASKS.filter(t => t.progress === 100).length;
  const inProgress = TASKS.filter(t => t.progress > 0 && t.progress < 100).length;
  const totalHours = TASKS.reduce((acc, t) => acc + t.hours, 0);

  const kpiData = [
    { title: "کل فعالیت‌های WBS", val: "۱,۶۲۴", sub: "تســک اورهال", icon: "clipboard-list", color: "text-blue-400", bg: "bg-blue-500/10" },
    { title: "فعالیت‌های تکمیل‌شده", val: completed + 412, sub: "تسک بسته شده", icon: "check-circle", color: "text-emerald-400", bg: "bg-emerald-500/10" },
    { title: "پرمیت‌های ایمنی فعال", val: PERMITS.length, sub: "HSE بدون حادثه", icon: "shield-alert", color: "text-amber-400", bg: "bg-amber-500/10" },
    { title: "نفر-ساعت مصرفی", val: (totalHours * 8) + " hr", sub: "شیفت‌های فعال", icon: "clock", color: "text-purple-400", bg: "bg-purple-500/10" }
  ];

  document.getElementById('kpiContainer').innerHTML = kpiData.map(k => `
    <div class="glass-card p-3 rounded-2xl border border-slate-700/60 flex items-center justify-between">
      <div>
        <p class="text-[11px] text-slate-400 font-medium">${k.title}</p>
        <p class="text-base sm:text-lg font-black text-white mt-0.5">${k.val}</p>
        <span class="text-[9px] text-slate-500">${k.sub}</span>
      </div>
      <div class="w-9 h-9 rounded-xl ${k.bg} flex items-center justify-center">
        <i data-lucide="${k.icon}" class="w-5 h-5 ${k.color}"></i>
      </div>
    </div>
  `).join('');
  lucide.createIcons();
}

function renderUnitBreakdown() {
  const units = [
    { name: "دیسیپلین مکانیک", prog: 42, color: "from-blue-500 to-indigo-600" },
    { name: "دیسیپلین برق و اتوماسیون", prog: 60, color: "from-emerald-500 to-teal-600" },
    { name: "دیسیپلین نسوز و کوره", prog: 30, color: "from-amber-500 to-orange-600" }
  ];

  document.getElementById('unitBreakdownContainer').innerHTML = units.map(u => `
    <div class="space-y-1">
      <div class="flex justify-between text-xs">
        <span class="text-slate-300 font-semibold">${u.name}</span>
        <span class="font-bold text-slate-200">${u.prog}٪</span>
      </div>
      <div class="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
        <div class="bg-gradient-to-l ${u.color} h-full rounded-full" style="width: ${u.prog}%"></div>
      </div>
    </div>
  `).join('');
}

function renderWbsTasks(filter = "all", query = "") {
  let list = TASKS;
  if (filter !== "all") list = list.filter(t => t.unit === filter);
  if (query.trim() !== "") list = list.filter(t => t.title.includes(query) || t.area.includes(query));

  document.getElementById('wbsTaskList').innerHTML = list.map(t => `
    <div class="glass-card p-3 rounded-xl border border-slate-700/60 hover:border-slate-600 transition space-y-2">
      <div class="flex items-start justify-between gap-2">
        <div class="space-y-1">
          <div class="flex items-center gap-2">
            <span class="text-[10px] font-mono font-bold px-1.5 py-0.5 rounded bg-slate-800 text-blue-400 border border-slate-700">WBS ${t.wbs}</span>
            <span class="text-[10px] px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 font-medium">${t.unit} • ${t.area}</span>
            ${t.needQc ? `<span class="text-[9px] px-1.5 py-0.5 rounded ${t.qcApproved ? 'bg-emerald-500/20 text-emerald-400' : 'bg-purple-500/20 text-purple-400'}">گیت QC مهندس خاکی</span>` : ''}
          </div>
          <h4 class="text-xs sm:text-sm font-bold text-white">${t.title}</h4>
        </div>
        <span class="text-xs font-black text-emerald-400 bg-emerald-950/40 border border-emerald-800/40 px-2 py-1 rounded-lg">${t.progress}٪</span>
      </div>

      <div class="flex items-center gap-3 pt-1 border-t border-slate-800/60">
        <input type="range" min="0" max="100" value="${t.progress}" onchange="updateProgress(${t.id}, this.value)" class="flex-1 accent-emerald-500 h-1.5 bg-slate-800 rounded-lg cursor-pointer">
        <button onclick="updateProgress(${t.id}, 100)" class="text-[10px] px-2 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold">۱۰۰٪ شد</button>
      </div>
    </div>
  `).join('');
  lucide.createIcons();
}

function updateProgress(id, val) {
  const task = TASKS.find(t => t.id === id);
  if (task) {
    if (task.needQc && val == 100 && !task.qcApproved && currentUser.role !== "واحد بازرسی فنی (QC)") {
      alert("خطای گیت QC: این فعالیت به دلیل الزام بازرسی فنی، بدون تایید مهندس خاکی نمی‌تواند به ۱۰۰٪ برسد.");
      task.progress = 95;
    } else {
      task.progress = parseInt(val);
      if (task.progress === 100) task.status = "completed";
    }
    renderWbsTasks(document.getElementById('wbsUnitFilter').value, document.getElementById('wbsSearchInput').value);
    renderKpis();
  }
}

function renderPermits() {
  document.getElementById('permitsList').innerHTML = PERMITS.map(p => `
    <div class="glass-card p-3 rounded-xl border border-slate-700/60 space-y-2">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-emerald-950/60 text-emerald-400 border border-emerald-800/60">${p.no}</span>
          <span class="text-xs font-bold text-white">${p.type}</span>
        </div>
        <span class="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400">معتبر و صادر شده</span>
      </div>
      <p class="text-xs text-slate-300"><span class="text-slate-500">محل اجرا:</span> ${p.location}</p>
      
      <div class="flex flex-wrap gap-2 text-[10px] text-slate-400 pt-1 border-t border-slate-800/60">
        ${p.o2 !== '-' ? `<span class="bg-slate-800 px-2 py-0.5 rounded text-slate-300">سنجش گاز: O2=${p.o2} | CO=${p.co}</span>` : ''}
        ${p.loto ? `<span class="bg-red-500/10 text-red-400 border border-red-500/20 px-2 py-0.5 rounded font-bold">LOTO برقی فعال</span>` : ''}
        <span class="bg-slate-800 px-2 py-0.5 rounded text-slate-400 truncate max-w-xs">الزامات: ${p.ppe}</span>
      </div>
    </div>
  `).join('');
}

function closeModal(id) {
  document.getElementById(id).classList.add('hidden');
}