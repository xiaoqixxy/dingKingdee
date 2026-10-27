import { useEffect, useId, useLayoutEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';

/** 可搜索的受控引用字段下拉，候选浮层挂到页面根节点以避免被表格裁切。 */
export default function ReferenceSearchSelect({ label, options, value, onChange }: {
  label: string; options: { value: string; label: string }[]; value: string; onChange: (value: string) => void;
}) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');
  const [active, setActive] = useState(0);
  const [position, setPosition] = useState({ left: 0, top: 0, width: 240, maxHeight: 240 });
  const wrapper = useRef<HTMLDivElement>(null);
  const listId = useId();
  // 受控值始终是选项键；关闭状态展示标签，打开状态展示临时搜索词。
  const selected = options.find(option => option.value === value);
  const filtered = options.filter(option => option.label.toLocaleLowerCase().includes(search.trim().toLocaleLowerCase()));
  /** 打开候选列表时清空搜索词并重置键盘高亮项。 */
  const show = () => { setSearch(''); setActive(0); setOpen(true); };
  /** 只提交候选项真实字段键，不把自由输入文本当作字段值。 */
  const choose = (key: string) => { onChange(key); setOpen(false); setSearch(''); };

  // 布局完成后立即定位浮层，并监听滚动与窗口变化，避免浮层与输入框错位。
  useLayoutEffect(() => {
    if (!open) return;
    /** 依据视口与控件位置计算浮层方向、宽度和可滚动高度。 */
    const update = () => {
      const rect = wrapper.current?.getBoundingClientRect();
      if (!rect) return;
      const below = window.innerHeight - rect.bottom - 8;
      const above = rect.top - 8;
      // 下方空间不足且上方更宽裕时向上展开，优先保证候选项在视口内可见。
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

  // 键盘高亮改变时仅滚动候选容器到最近位置，不滚动整页。
  useEffect(() => {
    if (open) document.getElementById(`${listId}-${active}`)?.scrollIntoView?.({ block: 'nearest' });
  }, [active, listId, open]);

  // 失焦到控件外部就收起浮层；候选项通过阻止鼠标默认焦点转移来保持可点击。
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
