import { useEffect, useId, useLayoutEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';

export default function ReferenceSearchSelect({ label, options, value, onChange }: {
  label: string; options: { value: string; label: string }[]; value: string; onChange: (value: string) => void;
}) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');
  const [active, setActive] = useState(0);
  const [position, setPosition] = useState({ left: 0, top: 0, width: 240, maxHeight: 240 });
  const wrapper = useRef<HTMLDivElement>(null);
  const listId = useId();
  const selected = options.find(option => option.value === value);
  const filtered = options.filter(option => option.label.toLocaleLowerCase().includes(search.trim().toLocaleLowerCase()));
  const show = () => { setSearch(''); setActive(0); setOpen(true); };
  const choose = (key: string) => { onChange(key); setOpen(false); setSearch(''); };

  useLayoutEffect(() => {
    if (!open) return;
    const update = () => {
      const rect = wrapper.current?.getBoundingClientRect();
      if (!rect) return;
      const below = window.innerHeight - rect.bottom - 8;
      const above = rect.top - 8;
      const upwards = below < 160 && above > below;
      const desiredHeight = Math.min(240, Math.max(44, filtered.length * 36 + 8));
      const maxHeight = Math.max(44, Math.min(desiredHeight, upwards ? above : below));
      const width = Math.min(Math.max(rect.width, 240), window.innerWidth - 16);
      setPosition({ left: Math.max(8, Math.min(rect.left, window.innerWidth - width - 8)),
        top: upwards ? Math.max(8, rect.top - maxHeight - 4) : rect.bottom + 4, width, maxHeight });
    };
    update();
    window.addEventListener('resize', update);
    window.addEventListener('scroll', update, true);
    return () => { window.removeEventListener('resize', update); window.removeEventListener('scroll', update, true); };
  }, [open, filtered.length]);

  useEffect(() => {
    if (open) document.getElementById(`${listId}-${active}`)?.scrollIntoView?.({ block: 'nearest' });
  }, [active, listId, open]);

  return <div className="reference-search-select" ref={wrapper} onBlur={event => {
    if (!event.currentTarget.contains(event.relatedTarget as Node)) { setOpen(false); setSearch(''); }
  }}>
    <input className="field-config-control" role="combobox" aria-label={label} aria-autocomplete="list"
      aria-controls={listId} aria-expanded={open} aria-activedescendant={open && filtered[active] ? `${listId}-${active}` : undefined}
      placeholder="输入名称或编码搜索" value={open ? search : selected?.label || ''}
      onFocus={show} onClick={() => { if (!open) show(); }}
      onChange={event => { setSearch(event.target.value); setActive(0); setOpen(true); }}
      onKeyDown={event => {
        if (event.key === 'Escape') { event.preventDefault(); setOpen(false); setSearch(''); }
        if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
          event.preventDefault(); setOpen(true);
          setActive(index => Math.max(0, Math.min(filtered.length - 1, index + (event.key === 'ArrowDown' ? 1 : -1))));
        }
        if (event.key === 'Enter' && open) { event.preventDefault(); if (filtered[active]) choose(filtered[active].value); }
      }} />
    <button type="button" className="reference-search-toggle" aria-label={`${label}展开或收起`} aria-expanded={open}
      onClick={() => open ? setOpen(false) : show()}>{open ? '▴' : '▾'}</button>
    {open && createPortal(<div className="reference-search-menu" id={listId} role="listbox" aria-label={`${label}候选项`}
      style={{ position: 'fixed', ...position }}>
      {filtered.map((option, index) => <div role="option" id={`${listId}-${index}`} key={option.value}
        aria-selected={option.value === value} className={index === active ? 'is-active' : ''}
        onMouseDown={event => event.preventDefault()} onMouseEnter={() => setActive(index)} onClick={() => choose(option.value)}>{option.label}</div>)}
      {filtered.length === 0 && <div className="reference-search-empty" role="status">没有匹配的字段</div>}
    </div>, document.body)}
  </div>;
}
