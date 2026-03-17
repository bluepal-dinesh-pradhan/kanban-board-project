import { Link } from 'react-router-dom'

const LandingPage = () => {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 relative overflow-hidden">
      <div className="absolute -top-32 -right-32 h-96 w-96 rounded-full bg-sky-200 blur-3xl opacity-60"></div>
      <div className="absolute -bottom-40 -left-32 h-[28rem] w-[28rem] rounded-full bg-emerald-200 blur-3xl opacity-60"></div>

      <header className="relative z-10">
        <nav className="max-w-6xl mx-auto px-6 py-6 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-sky-500 to-blue-700 flex items-center justify-center text-white font-bold">
              K
            </div>
            <span className="text-lg font-semibold tracking-tight">Kanban</span>
          </div>
          <div className="flex items-center gap-3">
            <Link
              to="/login"
              className="px-4 py-2 rounded-full text-sm font-semibold text-slate-700 hover:text-slate-900 transition-colors"
            >
              Log in
            </Link>
            <Link
              to="/register"
              className="px-4 py-2 rounded-full text-sm font-semibold text-white bg-[#0052CC] hover:bg-[#0747A6] transition-colors"
            >
              Get Kanban for free
            </Link>
          </div>
        </nav>
      </header>

      <main className="relative z-10">
        <section className="max-w-6xl mx-auto px-6 pt-8 pb-16 grid lg:grid-cols-[1.1fr_0.9fr] gap-12 items-center">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-sky-700 mb-4">
              Visualize work. Ship faster.
            </p>
            <h1 className="text-4xl md:text-5xl font-extrabold leading-tight text-slate-900 mb-6">
              Organize everything, from the big picture to the tiny details.
            </h1>
            <p className="text-lg text-slate-600 mb-8">
              Kanban brings your team, tasks, and timelines into one calm, colorful board.
              Keep projects moving with clarity, ownership, and momentum.
            </p>
            <div className="flex flex-col sm:flex-row sm:items-center gap-4">
              <Link
                to="/register"
                className="px-6 py-3 rounded-full text-base font-semibold text-white bg-[#0052CC] hover:bg-[#0747A6] transition-colors text-center"
              >
                Sign up — it&apos;s free!
              </Link>
              <Link
                to="/login"
                className="px-6 py-3 rounded-full text-base font-semibold text-[#0052CC] border border-[#0052CC] hover:bg-blue-50 transition-colors text-center"
              >
                Log in
              </Link>
            </div>
            <div className="mt-8 flex items-center gap-6 text-sm text-slate-500">
              <span>Trusted by focused teams</span>
              <span className="h-1 w-1 rounded-full bg-slate-300"></span>
              <span>Built for speed and flow</span>
            </div>
          </div>

          <div className="relative">
            <div className="absolute inset-0 bg-gradient-to-br from-sky-100 to-emerald-100 rounded-3xl rotate-2"></div>
            <div className="relative bg-white rounded-3xl shadow-2xl p-6 border border-slate-100">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <p className="text-xs uppercase tracking-widest text-slate-400">Today</p>
                  <h3 className="text-xl font-semibold">Product Launch</h3>
                </div>
                <span className="text-xs px-3 py-1 rounded-full bg-emerald-100 text-emerald-700 font-medium">
                  On track
                </span>
              </div>
              <div className="space-y-4">
                {[
                  { label: 'Research', color: 'bg-sky-500', progress: 'w-10/12' },
                  { label: 'Design', color: 'bg-indigo-500', progress: 'w-8/12' },
                  { label: 'Build', color: 'bg-amber-500', progress: 'w-6/12' },
                ].map((item) => (
                  <div key={item.label}>
                    <div className="flex items-center justify-between text-sm font-medium text-slate-600">
                      <span>{item.label}</span>
                      <span>In progress</span>
                    </div>
                    <div className="mt-2 h-2 rounded-full bg-slate-100">
                      <div className={`h-2 rounded-full ${item.color} ${item.progress}`}></div>
                    </div>
                  </div>
                ))}
              </div>
              <div className="mt-6 grid grid-cols-2 gap-3">
                {['Draft scope', 'Align stakeholders', 'Sprint review', 'QA checklist'].map((task) => (
                  <div
                    key={task}
                    className="rounded-2xl border border-slate-100 bg-slate-50 px-3 py-3 text-sm text-slate-600"
                  >
                    {task}
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>

        <section className="max-w-6xl mx-auto px-6 pb-16">
          <div className="grid md:grid-cols-3 gap-6">
            {[
              {
                title: 'Capture every idea',
                text: 'Create lists for any workflow and drag cards as work evolves.',
              },
              {
                title: 'Stay aligned',
                text: 'Assign owners, set due dates, and keep your team in sync.',
              },
              {
                title: 'Move with clarity',
                text: 'Spot blockers early and keep momentum across every sprint.',
              },
            ].map((feature) => (
              <div
                key={feature.title}
                className="rounded-3xl bg-white border border-slate-100 p-6 shadow-sm hover:shadow-md transition-shadow"
              >
                <h3 className="text-lg font-semibold mb-2">{feature.title}</h3>
                <p className="text-slate-600 text-sm">{feature.text}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="max-w-6xl mx-auto px-6 py-16">
          <div className="grid lg:grid-cols-[1fr_1.1fr] gap-12 items-center">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.2em] text-sky-700 mb-3">
                Your productivity powerhouse
              </p>
              <h2 className="text-3xl md:text-4xl font-bold text-slate-900 mb-5">
                Boards, cards, and collaboration designed for momentum.
              </h2>
              <p className="text-slate-600 text-lg mb-6">
                Build a system that matches how your team thinks. Break down work into cards, move them
                through stages, and keep every stakeholder aligned in real time.
              </p>
              <div className="space-y-4">
                {[
                  {
                    title: 'Boards that stay focused',
                    text: 'Organize work by team, sprint, or initiative with clear ownership.',
                  },
                  {
                    title: 'Cards with real context',
                    text: 'Add checklists, attachments, and due dates without losing clarity.',
                  },
                  {
                    title: 'Collaboration that flows',
                    text: 'Mention teammates, leave comments, and keep decisions visible.',
                  },
                ].map((item) => (
                  <div key={item.title} className="rounded-2xl border border-slate-100 bg-white p-4 shadow-sm">
                    <h3 className="text-base font-semibold text-slate-900">{item.title}</h3>
                    <p className="text-sm text-slate-600 mt-1">{item.text}</p>
                  </div>
                ))}
              </div>
            </div>

            <div className="relative">
              <div className="absolute inset-0 bg-gradient-to-br from-sky-100 to-blue-50 rounded-3xl rotate-1"></div>
              <div className="relative bg-white rounded-3xl border border-slate-100 shadow-xl p-6">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <p className="text-xs uppercase tracking-widest text-slate-400">Board</p>
                    <h3 className="text-lg font-semibold">Launch Campaign</h3>
                  </div>
                  <span className="text-xs px-3 py-1 rounded-full bg-sky-100 text-sky-700 font-medium">
                    12 cards
                  </span>
                </div>
                <div className="grid grid-cols-3 gap-3 text-xs text-slate-600">
                  {[
                    { title: 'To do', color: 'bg-slate-100' },
                    { title: 'In progress', color: 'bg-sky-100' },
                    { title: 'Done', color: 'bg-emerald-100' },
                  ].map((col) => (
                    <div key={col.title} className="space-y-2">
                      <div className="font-semibold text-slate-700">{col.title}</div>
                      {['Brief', 'Assets', 'Launch'].map((card, idx) => (
                        <div
                          key={`${col.title}-${card}`}
                          className={`rounded-xl px-2 py-2 ${col.color} ${idx === 2 ? 'opacity-70' : ''}`}
                        >
                          <div className="text-[11px] font-semibold text-slate-700">{card}</div>
                          <div className="mt-1 h-1.5 w-1/2 rounded-full bg-slate-300"></div>
                        </div>
                      ))}
                    </div>
                  ))}
                </div>
                <div className="mt-5 rounded-2xl bg-slate-50 border border-slate-100 p-4">
                  <div className="flex items-center justify-between text-sm font-medium text-slate-700">
                    <span>Collaboration</span>
                    <span className="text-emerald-600">Live</span>
                  </div>
                  <div className="mt-3 flex items-center gap-2">
                    {['A', 'M', 'J', 'S'].map((initial) => (
                      <div
                        key={initial}
                        className="h-8 w-8 rounded-full bg-sky-600 text-white flex items-center justify-center text-xs font-semibold"
                      >
                        {initial}
                      </div>
                    ))}
                    <div className="text-xs text-slate-500">4 collaborators active</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="max-w-6xl mx-auto px-6 py-16">
          <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4 mb-8">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.2em] text-sky-700 mb-2">
                Workflows for any project
              </p>
              <h2 className="text-3xl font-bold text-slate-900">Templates tailored to your reality.</h2>
            </div>
            <p className="text-slate-600 max-w-xl">
              From personal productivity to enterprise programs, Kanban adapts to every workflow.
            </p>
          </div>
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {[
              { title: 'Project management', text: 'Plan milestones, assign owners, and monitor delivery.' },
              { title: 'Team collaboration', text: 'Align stakeholders with shared visibility.' },
              { title: 'Task tracking', text: 'Capture action items and keep priorities clear.' },
              { title: 'Sprint planning', text: 'Break down work and keep iteration flow steady.' },
              { title: 'Personal productivity', text: 'Organize daily work without the clutter.' },
              { title: 'Remote teams', text: 'Stay connected across time zones and tools.' },
            ].map((item) => (
              <div
                key={item.title}
                className="rounded-3xl border border-slate-100 bg-white p-6 shadow-sm hover:shadow-md transition-shadow"
              >
                <h3 className="text-lg font-semibold text-slate-900 mb-2">{item.title}</h3>
                <p className="text-sm text-slate-600">{item.text}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="max-w-6xl mx-auto px-6 py-16">
          <div className="text-center mb-10">
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-sky-700 mb-2">
              See how it works
            </p>
            <h2 className="text-3xl font-bold text-slate-900">Move from idea to impact in four steps.</h2>
          </div>
          <div className="relative">
            <div className="absolute left-6 right-6 top-6 h-0.5 bg-slate-200 hidden md:block"></div>
            <div className="grid md:grid-cols-4 gap-6">
              {[
                'Create board',
                'Add tasks',
                'Invite team',
                'Track progress',
              ].map((step, index) => (
                <div key={step} className="relative flex flex-col items-center text-center">
                  <div className="h-12 w-12 rounded-full bg-[#0052CC] text-white flex items-center justify-center font-semibold text-lg mb-3">
                    {index + 1}
                  </div>
                  <h3 className="text-base font-semibold text-slate-900 mb-2">{step}</h3>
                  <p className="text-sm text-slate-600">
                    {index === 0 && 'Set up a board that mirrors your workflow.'}
                    {index === 1 && 'Capture tasks, owners, and due dates.'}
                    {index === 2 && 'Bring teammates in with a single invite.'}
                    {index === 3 && 'Follow progress with clear status changes.'}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </section>

        <section className="max-w-6xl mx-auto px-6 py-16">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.2em] text-sky-700 mb-2">
                Built with modern technology
              </p>
              <h2 className="text-3xl font-bold text-slate-900">Reliable, fast, and future-proof.</h2>
            </div>
            <div className="flex flex-wrap gap-3">
              {[
                { label: 'React', color: 'bg-sky-100 text-sky-700' },
                { label: 'Spring Boot', color: 'bg-emerald-100 text-emerald-700' },
                { label: 'PostgreSQL', color: 'bg-indigo-100 text-indigo-700' },
                { label: 'Tailwind', color: 'bg-cyan-100 text-cyan-700' },
                { label: 'JWT', color: 'bg-amber-100 text-amber-700' },
                { label: 'Docker', color: 'bg-blue-100 text-blue-700' },
              ].map((badge) => (
                <span
                  key={badge.label}
                  className={`px-4 py-2 rounded-full text-sm font-semibold ${badge.color}`}
                >
                  {badge.label}
                </span>
              ))}
            </div>
          </div>
        </section>

        <section className="max-w-6xl mx-auto px-6 pb-20">
          <div className="rounded-3xl bg-gradient-to-r from-[#0052CC] to-[#2684FF] px-8 py-12 text-white flex flex-col md:flex-row md:items-center md:justify-between gap-6">
            <div>
              <h2 className="text-3xl font-bold mb-2">Start organizing your work today</h2>
              <p className="text-white/80">Bring clarity to every project with Kanban.</p>
            </div>
            <div className="flex flex-col sm:flex-row gap-4">
              <Link
                to="/register"
                className="px-6 py-3 rounded-full text-base font-semibold bg-white text-[#0052CC] hover:bg-blue-50 transition-colors text-center"
              >
                Sign up — it&apos;s free!
              </Link>
              <Link
                to="/login"
                className="px-6 py-3 rounded-full text-base font-semibold border border-white text-white hover:bg-white/10 transition-colors text-center"
              >
                Log in
              </Link>
            </div>
          </div>
        </section>

        <footer className="bg-slate-900 text-slate-200">
          <div className="max-w-6xl mx-auto px-6 py-12 grid md:grid-cols-3 gap-8">
            <div>
              <div className="flex items-center gap-3 mb-4">
                <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-sky-500 to-blue-700 flex items-center justify-center text-white font-bold">
                  K
                </div>
                <span className="text-lg font-semibold">Kanban</span>
              </div>
              <p className="text-sm text-slate-400">
                The collaborative workspace for teams that want clarity, speed, and a calm workflow.
              </p>
            </div>
            <div>
              <h3 className="text-sm font-semibold uppercase tracking-widest text-slate-400 mb-3">
                Product
              </h3>
              <ul className="space-y-2 text-sm text-slate-300">
                <li>Boards &amp; cards</li>
                <li>Automation</li>
                <li>Integrations</li>
                <li>Security</li>
              </ul>
            </div>
            <div>
              <h3 className="text-sm font-semibold uppercase tracking-widest text-slate-400 mb-3">
                About
              </h3>
              <ul className="space-y-2 text-sm text-slate-300">
                <li>Company</li>
                <li>Careers</li>
                <li>Press</li>
                <li>Contact</li>
              </ul>
            </div>
          </div>
        </footer>
      </main>
    </div>
  )
}

export default LandingPage
