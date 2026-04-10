// address-form.js
(function () {
  console.log("[ADDR] script loaded");

  window.addEventListener("DOMContentLoaded", async function () {
    const holder = document.querySelector('[data-vnjson-url]');
    const DATA_URL = holder ? holder.getAttribute('data-vnjson-url') : '/web/assets/data/vn-divisions.json';
    console.log("[ADDR] DATA_URL =", DATA_URL);

    const $p = document.getElementById('provinceSelect');
    const $d = document.getElementById('districtSelect');
    const $w = document.getElementById('wardSelect');
    if (!$p || !$d || !$w) {
      console.error("[ADDR] Missing selects in DOM");
      return;
    }

    const hProvince = document.querySelector('input[name="province"]');
    const hDistrict = document.querySelector('input[name="district"]');
    const hWard     = document.querySelector('input[name="ward"]');
    const prefillProvince = (holder?.getAttribute('data-prefill-province') || '').trim();
    const prefillDistrict = (holder?.getAttribute('data-prefill-district') || '').trim();
    const prefillWard = (holder?.getAttribute('data-prefill-ward') || '').trim();

    let raw, usedFallback = false;
    try {
      const resp = await fetch(DATA_URL, { headers: { 'Accept': 'application/json' }});
      console.log("[ADDR] fetch status =", resp.status);
      if (!resp.ok) throw new Error('HTTP ' + resp.status);
      raw = await resp.json();
    } catch (e) {
      console.error("[ADDR] Cannot load JSON, using fallback:", e);
      raw = {
        provinces: [{code:'79', name:'TP. Hồ Chí Minh'},{code:'01', name:'TP. Hà Nội'}],
        districts: {'79':[ {code:'769',name:'TP Thủ Đức'} ], '01':[ {code:'001',name:'Quận Ba Đình'} ]},
        wards: {'769':[ {code:'26734',name:'Linh Trung'} ], '001':[ {code:'00001',name:'Phúc Xá'} ]}
      };
      usedFallback = true;
    }

    const norm = { provinces: [], districts: {}, wards: {} };
    if (Array.isArray(raw)) {
      norm.provinces = raw.map(p => ({ code: String(p.code), name: p.name }));
      raw.forEach(p => {
        const pCode = String(p.code);
        const dList = (p.districts||[]).map(d => ({ code:String(d.code), name:d.name }));
        norm.districts[pCode] = dList;
        (p.districts||[]).forEach(d=>{
          const dCode = String(d.code);
          norm.wards[dCode] = (d.wards||[]).map(w=>({code:String(w.code),name:w.name}));
        });
      });
    } else if (raw && raw.provinces && raw.districts && raw.wards) {
      norm.provinces = raw.provinces.map(p=>({code:String(p.code),name:p.name}));
      Object.keys(raw.districts).forEach(pCode=>{
        norm.districts[String(pCode)] = (raw.districts[pCode]||[]).map(d=>({code:String(d.code),name:d.name}));
      });
      Object.keys(raw.wards).forEach(dCode=>{
        norm.wards[String(dCode)] = (raw.wards[dCode]||[]).map(w=>({code:String(w.code),name:w.name}));
      });
    } else {
      console.error("[ADDR] Unsupported JSON schema:", raw);
      alert("Dữ liệu địa giới không đúng định dạng.");
      return;
    }

    const fillOptions = (sel, arr, placeholder) => {
      sel.innerHTML = '';
      const opt0 = document.createElement('option');
      opt0.value = ''; opt0.textContent = placeholder; opt0.disabled = true; opt0.selected = true;
      sel.appendChild(opt0);
      (arr||[]).forEach(it=>{
        const o = document.createElement('option');
        o.value = it.code; o.textContent = it.name;
        sel.appendChild(o);
      });
    };

    const normalizeName = (text) => {
      return String(text || '')
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/^(tp\.?|thanh pho|tinh)\s+/g, '')
        .replace(/^(quan|huyen|thi xa|thanh pho)\s+/g, '')
        .replace(/^(phuong|xa|thi tran)\s+/g, '')
        .replace(/\s+/g, ' ')
        .trim();
    };

    const selectByText = (selectElement, labelText) => {
      if (!selectElement || !labelText) return false;
      const expected = normalizeName(labelText);
      const matchedOption = Array.from(selectElement.options).find((option) => {
        return normalizeName(option.textContent) === expected;
      });
      if (!matchedOption) return false;
      selectElement.value = matchedOption.value;
      selectElement.dispatchEvent(new Event('change', { bubbles: true }));
      return true;
    };

    // Provinces
    fillOptions($p, norm.provinces, '-- Chọn Tỉnh/Thành --');
    console.log("[ADDR] provinces =", norm.provinces.length, "fallback =", usedFallback);

    $p.addEventListener('change', () => {
      const pCode = $p.value;
      const districts = norm.districts[pCode] || [];
      fillOptions($d, districts, '-- Chọn Quận/Huyện --');
      $d.disabled = districts.length === 0;

      const pObj = norm.provinces.find(x=>x.code===pCode);
      if (hProvince) hProvince.value = pObj ? pObj.name : '';

      fillOptions($w, [], '-- Chọn Phường/Xã --');
      $w.disabled = true;
      if (hDistrict) hDistrict.value = '';
      if (hWard) hWard.value = '';
      console.log("[ADDR] province changed:", pCode, "districts:", districts.length);
    });

    $d.addEventListener('change', () => {
      const dCode = $d.value;
      const wards = norm.wards[dCode] || [];
      fillOptions($w, wards, '-- Chọn Phường/Xã --');
      $w.disabled = wards.length === 0;

      const pCode = $p.value;
      const dObj = (norm.districts[pCode]||[]).find(x=>x.code===dCode);
      if (hDistrict) hDistrict.value = dObj ? dObj.name : '';

      if (hWard) hWard.value = '';
      console.log("[ADDR] district changed:", dCode, "wards:", wards.length);
    });

    $w.addEventListener('change', () => {
      const dCode = $d.value;
      const wObj = (norm.wards[dCode]||[]).find(x=>x.code===$w.value);
      if (hWard) hWard.value = wObj ? wObj.name : '';
      console.log("[ADDR] ward changed:", $w.value);
    });

    // Prefill for edit form (province -> district -> ward)
    if (prefillProvince && selectByText($p, prefillProvince)) {
      if (prefillDistrict && selectByText($d, prefillDistrict)) {
        if (prefillWard) {
          selectByText($w, prefillWard);
        }
      }
    }
  });
})();
